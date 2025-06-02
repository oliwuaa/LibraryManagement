package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UserControllerSimulation extends Simulation {

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

  def withAuth = exec(session => session.set("Authorization", s"Bearer ${session("jwt").as[String]}"))

  def authorizedGet(name: String, url: String) = {
    exec(withAuth)
      .exec(
        http(name)
          .get(url)
          .header("Authorization", "${Authorization}")
          .check(status.saveAs("httpStatus"))
      )
  }

  def authorizedPost(name: String, url: String, body: String) = {
    exec(withAuth)
      .exec(
        http(name)
          .post(url)
          .header("Authorization", "${Authorization}")
          .body(StringBody(body)).asJson
          .check(status.saveAs("httpStatus"))
      )
  }

  def authorizedPut(name: String, url: String, body: String) = {
    exec(withAuth)
      .exec(
        http(name)
          .put(url)
          .header("Authorization", "${Authorization}")
          .body(StringBody(body)).asJson
          .check(status.saveAs("httpStatus"))
      )
  }

  def authorizedPatch(name: String, url: String, body: String) = {
    exec(withAuth)
      .exec(
        http(name)
          .patch(url)
          .header("Authorization", "${Authorization}")
          .body(StringBody(body)).asJson
          .check(status.saveAs("httpStatus"))
      )
  }

  def authorizedDelete(name: String, url: String) = {
    exec(withAuth)
      .exec(
        http(name)
          .delete(url)
          .header("Authorization", "${Authorization}")
          .check(status.saveAs("httpStatus"))
      )
  }

  val exampleUserId = 6L
  val exampleLibraryId = 1L

  val adminScenario = scenario("Admin UserController Actions")
    .exec(login("admin"))
    .pause(1)
    .exec(authorizedGet("Get all users", "/users"))
    .pause(1)
    .exec(authorizedGet("Get user by id", s"/users/$exampleUserId"))
    .pause(1)
    .exec(authorizedGet("Search users", "/users/search?name=John"))
    .pause(1)
    .exec(authorizedGet("Get librarians by library", s"/users/library/$exampleLibraryId/librarians"))
    .pause(1)
    .exec(authorizedPut("Update user", s"/users/$exampleUserId", """{"id":6,"email":"new@email.com","name":"Updated","surname":"User","role":"USER","library":null}"""))
    .pause(1)
    .exec(authorizedPatch("Change user role", s"/users/$exampleUserId?libraryId=$exampleLibraryId", "\"LIBRARIAN\""))
    .pause(1)
    .exec(authorizedDelete("Delete user", s"/users/$exampleUserId"))
    .pause(1)

  val librarianScenario = scenario("Librarian UserController Actions")
    .exec(login("librarian"))
    .pause(1)
    .exec(authorizedGet("Get self by ID", s"/users/$exampleUserId"))
    .pause(1)
    .exec(authorizedGet("Search users", "/users/search?email=librarian"))
    .pause(1)

  val userScenario = scenario("User UserController Actions")
    .exec(login("user"))
    .pause(1)
    .exec(authorizedGet("Get current user info", "/users/me"))
    .pause(1)
    .exec(authorizedGet("Get self by ID", s"/users/$exampleUserId"))

  setUp(
    adminScenario.inject(rampUsers(10).during(10.seconds)),
    librarianScenario.inject(rampUsers(10).during(10.seconds)),
    userScenario.inject(rampUsers(10).during(10.seconds))
  ).protocols(httpProtocol)
}