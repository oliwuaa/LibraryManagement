package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LibraryControllerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val login = exec(
    http("Login")
      .post("/auth/login")
      .body(StringBody("""{ "email": "librarian@example.com", "password": "librarian" }""")).asJson
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

  val libraryId = "1"
  val searchName = "Central"
  val searchAddress = "Main St"

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

  val scn = scenario("Library Controller Endpoints (Authenticated & Resilient)")
    .exec(login)
    .pause(2)
    .exec(authorizedGet("Get Library by ID", s"/libraries/$libraryId"))
    .pause(2)
    .exec(authorizedGet("Get All Libraries", "/libraries"))
    .pause(2)
    .exec(authorizedGet("Search Libraries", s"/libraries/search?name=$searchName&address=$searchAddress"))
    .pause(2)
    .exec(authorizedGet("Get My Library", "/libraries/me"))

  setUp(
    scn.inject(rampUsers(20).during(10.seconds))
  ).protocols(httpProtocol)
}
