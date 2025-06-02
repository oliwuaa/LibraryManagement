package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CopyControllerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val login = exec(
    http("Login")
      .post("/auth/login")
      .body(StringBody("""{ "email": "user@example.com", "password": "user" }""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.accessToken").exists.saveAs("jwt"))
  ).exitHereIfFailed
    .exec { session =>
      if (!session.contains("jwt")) {
        println(s"[User-${session.userId}] Login failed, JWT not found")
      } else {
        println(s"[User-${session.userId}] Login succeeded, JWT found")
      }
      session
    }

  def ensureLoggedIn = doIf(session => !session.contains("jwt")) {
    login
  }

  val copyId = "1"
  val libraryId = "1"
  val bookId = "1"

  def authorizedGet(name: String, url: String) = {
    doIfOrElse(session => session.contains("jwt")) {
      exec(session => {
        val jwt = session("jwt").as[String]
        session.set("Authorization", s"Bearer $jwt")
      })
        .exec(
          http(name)
            .get(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec { session =>
          println(s"[User-${session.userId}] Request '$name' to '$url' returned status: ${session("httpStatus").as[Int]}")
          session
        }
    } {
      exec(login)
        .exec(session => {
          val jwt = session("jwt").as[String]
          session.set("Authorization", s"Bearer $jwt")
        })
        .exec(
          http(name + " (retry after login)")
            .get(url)
            .header("Authorization", "${Authorization}")
            .check(status.saveAs("httpStatus"))
        )
        .exec { session =>
          println(s"[User-${session.userId}] Retry request '$name' to '$url' returned status: ${session("httpStatus").as[Int]}")
          session
        }
    }
  }

  val scn = scenario("Copy Controller Endpoints (Authenticated & Resilient)")
    .exec(login)
    .pause(2)
    .exec(authorizedGet("Get Copy by ID", s"/copies/$copyId"))
    .pause(2)
    .exec(authorizedGet("Get All Copies", "/copies"))
    .pause(2)
    .exec(authorizedGet("Get Copies by Library ID", s"/copies/library/$libraryId"))
    .pause(2)
    .exec(authorizedGet("Get Available Copies by Book ID in Library", s"/copies/library/$libraryId/book/$bookId"))
    .pause(2)
    .exec(authorizedGet("Get Available Copies by Library ID", s"/copies/library/$libraryId/available"))
    .pause(2)
    .exec(authorizedGet("Get Copies by Book ID", s"/copies/book/$bookId"))
    .pause(2)
    .exec(authorizedGet("Get Available Copies by Book ID", s"/copies/book/$bookId/available"))
    .pause(2)
    .exec(authorizedGet("Get All Available Copies", "/copies/available"))

  setUp(
    scn.inject(rampUsers(15).during(10.seconds))
  ).protocols(httpProtocol)
}
