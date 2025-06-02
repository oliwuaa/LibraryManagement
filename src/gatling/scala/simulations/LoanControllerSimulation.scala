package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoanControllerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")


  val users = Map(
    "admin" -> ("admin@example.com", "admin"),
    "librarian" -> ("librarian@example.com", "librarian"),
    "user" -> ("user@example.com", "user")
  )

  def login(role: String) = exec(
    http(s"Login $role")
      .post("/auth/login")
      .body(StringBody(session => s"""{"email":"${users(role)._1}","password":"${users(role)._2}"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.accessToken").exists.saveAs("jwt"))
  ).exitHereIfFailed
    .exec(session => {
      if (!session.contains("jwt")) {
        println(s"[User-${session.userId}] Login $role failed, JWT not found")
      } else {
        println(s"[User-${session.userId}] Login $role succeeded, JWT found")
      }
      session
    })

  def withAuth = exec(session => {
    val jwt = session("jwt").as[String]
    session.set("Authorization", s"Bearer $jwt")
  })

  def authorizedGet(name: String, url: String) = {
    doIfOrElse(session => session.contains("jwt"))(
      exec(withAuth)
        .exec(
          http(name)
            .get(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] GET '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )(
      exec(login("user")) // retry login
        .exec(withAuth)
        .exec(
          http(name + " (retry after login)")
            .get(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] Retry GET '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )
  }

  def authorizedPost(name: String, url: String, body: String) = {
    doIfOrElse(session => session.contains("jwt"))(
      exec(withAuth)
        .exec(
          http(name)
            .post(url)
            .header("Authorization", "${Authorization}")
            .body(StringBody(body)).asJson
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] POST '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )(
      exec(login("user"))
        .exec(withAuth)
        .exec(
          http(name + " (retry after login)")
            .post(url)
            .header("Authorization", "${Authorization}")
            .body(StringBody(body)).asJson
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] Retry POST '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )
  }

  val exampleUserId: Long = 5
  val exampleLoanId: Long = 10
  val exampleCopyId: Long = 12
  val exampleLibraryId: Long = 1
  val newReturnDate = "2025-09-30"

  val adminScenario = scenario("Admin Loan Controller Actions")
    .exec(login("admin"))
    .pause(1)
    .exec(authorizedGet("Get all loans", "/loans"))
    .pause(1)
    .exec(authorizedGet("Get loans by library", s"/loans/library/$exampleLibraryId"))
    .pause(1)
    .exec(authorizedGet("Get loan by id", s"/loans/$exampleLoanId"))
    .pause(1)
    .exec(authorizedGet("Get user loans", s"/loans/user/$exampleUserId"))
    .pause(1)
    .exec(authorizedPost("Create loan", "/loans", s"userId=$exampleUserId&copyId=$exampleCopyId"))
    .pause(1)
    .exec(authorizedPost("Return loan", s"/loans/$exampleLoanId/return", ""))
    .pause(1)
    .exec(authorizedPost("Extend loan", s"/loans/$exampleLoanId/extend", s"returnDate=$newReturnDate"))
    .pause(1)

  val librarianScenario = scenario("Librarian Loan Controller Actions")
    .exec(login("librarian"))
    .pause(1)
    .exec(authorizedGet("Get loans by library", s"/loans/library/$exampleLibraryId"))
    .pause(1)
    .exec(authorizedGet("Get loan by id", s"/loans/$exampleLoanId"))
    .pause(1)
    .exec(authorizedGet("Get user loans", s"/loans/user/$exampleUserId"))
    .pause(1)
    .exec(authorizedPost("Create loan", "/loans", s"userId=$exampleUserId&copyId=$exampleCopyId"))
    .pause(1)
    .exec(authorizedPost("Return loan", s"/loans/$exampleLoanId/return", ""))
    .pause(1)
    .exec(authorizedPost("Extend loan", s"/loans/$exampleLoanId/extend", s"returnDate=$newReturnDate"))
    .pause(1)

  val userScenario = scenario("User Loan Controller Actions")
    .exec(login("user"))
    .pause(1)
    .exec(authorizedGet("Get my loans", "/loans/me"))
    .pause(1)
    .exec(authorizedGet("Get my active loans", "/loans/me/active"))
    .pause(1)
    .exec(authorizedGet("Get loan by id", s"/loans/$exampleLoanId"))
    .pause(1)

  setUp(
    adminScenario.inject(rampUsers(10).during(10.seconds)),
    librarianScenario.inject(rampUsers(10).during(10.seconds)),
    userScenario.inject(rampUsers(10).during(10.seconds))
  ).protocols(httpProtocol)
}