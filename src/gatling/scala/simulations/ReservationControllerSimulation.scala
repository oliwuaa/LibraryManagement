package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ReservationControllerSimulation extends Simulation {

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
      exec(login("user"))
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

  def authorizedPut(name: String, url: String) = {
    doIfOrElse(session => session.contains("jwt"))(
      exec(withAuth)
        .exec(
          http(name)
            .put(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] PUT '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )(
      exec(login("user"))
        .exec(withAuth)
        .exec(
          http(name + " (retry after login)")
            .put(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec(session => {
          println(s"[User-${session.userId}] Retry PUT '$name' ($url) status: ${session("httpStatus").as[Int]}")
          session
        })
    )
  }

  val exampleUserId: Long = 12
  val exampleReservationId: Long = 42
  val exampleCopyId: Long = 3
  val exampleLibraryId: Long = 1

  val adminScenario = scenario("Admin Reservation Controller Actions")
    .exec(login("admin"))
    .pause(1)
    .exec(authorizedGet("Get all reservations", "/reservations"))
    .pause(1)
    .exec(authorizedGet("Get reservations by library", s"/reservations/library/$exampleLibraryId"))
    .pause(1)
    .exec(authorizedGet("Get reservation by id", s"/reservations/$exampleReservationId"))
    .pause(1)
    .exec(authorizedGet("Get user reservations", s"/reservations/user/$exampleUserId"))
    .pause(1)
    .exec(authorizedPost("Make book reservation", "/reservations", s"copyId=$exampleCopyId"))
    .pause(1)
    .exec(authorizedPut("Cancel reservation", s"/reservations/$exampleReservationId/cancel"))
    .pause(1)

  val librarianScenario = scenario("Librarian Reservation Controller Actions")
    .exec(login("librarian"))
    .pause(1)
    .exec(authorizedGet("Get reservations by library", s"/reservations/library/$exampleLibraryId"))
    .pause(1)
    .exec(authorizedGet("Get reservation by id", s"/reservations/$exampleReservationId"))
    .pause(1)
    .exec(authorizedGet("Get user reservations", s"/reservations/user/$exampleUserId"))
    .pause(1)
    .exec(authorizedPost("Make book reservation", "/reservations", s"copyId=$exampleCopyId"))
    .pause(1)
    .exec(authorizedPut("Cancel reservation", s"/reservations/$exampleReservationId/cancel"))
    .pause(1)

  val userScenario = scenario("User Reservation Controller Actions")
    .exec(login("user"))
    .pause(1)
    .exec(authorizedGet("Get my reservations", "/reservations/me"))
    .pause(1)
    .exec(authorizedGet("Get my active reservations", "/reservations/me/active"))
    .pause(1)
    .exec(authorizedGet("Get reservation by id", s"/reservations/$exampleReservationId"))
    .pause(1)
    .exec(authorizedPost("Make book reservation", "/reservations", s"copyId=$exampleCopyId"))
    .pause(1)
    .exec(authorizedPut("Cancel reservation", s"/reservations/$exampleReservationId/cancel"))
    .pause(1)

  setUp(
    adminScenario.inject(rampUsers(10).during(10.seconds)),
    librarianScenario.inject(rampUsers(10).during(10.seconds)),
    userScenario.inject(rampUsers(10).during(10.seconds))
  ).protocols(httpProtocol)
}
