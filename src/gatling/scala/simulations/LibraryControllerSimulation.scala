package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv

class LibraryControllerSimulation extends Simulation {

  val dotenv = Dotenv.configure()
    .directory(".")
    .ignoreIfMalformed()
    .ignoreIfMissing()
    .load()

  val token = dotenv.get("JWT_SECRET")

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader(s"Bearer $token")

  val libraryId = "1"
  val searchName = "Central"
  val searchAddress = "Main St"

  val getLibraryById = exec(
    http("Get Library by ID")
      .get(s"/libraries/$libraryId")
      .check(status.is(200))
  )

  val getAllLibraries = exec(
    http("Get All Libraries")
      .get("/libraries")
      .check(status.is(200))
  )

  val searchLibraries = exec(
    http("Search Libraries")
      .get(s"/libraries/search?name=$searchName&address=$searchAddress")
      .check(status.is(200))
  )

  val getMyLibrary = exec(
    http("Get My Library")
      .get("/libraries/me")
      .check(status.is(403))
  )

  val scn = scenario("Library Controller Endpoints")
    .exec(getLibraryById)
    .pause(1)
    .exec(getAllLibraries)
    .pause(1)
    .exec(searchLibraries)
    .pause(1)
    .exec(getMyLibrary)

  setUp(
    scn.inject(rampUsers(20).during(5.seconds))
  ).protocols(httpProtocol)
}
