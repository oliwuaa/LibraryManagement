package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv

class CopyControllerSimulation extends Simulation {

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

  val copyId = "1"
  val libraryId = "1"
  val bookId = "1"

  val getCopyById = exec(
    http("Get Copy by ID")
      .get(s"/copies/$copyId")
      .check(status.is(200))
  )

  val getAllCopies = exec(
    http("Get All Copies")
      .get("/copies")
      .check(status.is(200))
  )

  val getCopiesByLibraryId = exec(
    http("Get Copies by Library ID")
      .get(s"/copies/library/$libraryId")
      .check(status.is(200))
  )

  val getAvailableCopiesByBookIdInLibrary = exec(
    http("Get Available Copies by Book ID in Library")
      .get(s"/copies/library/$libraryId/book/$bookId")
      .check(status.is(200))
  )

  val getAvailableCopiesByLibraryId = exec(
    http("Get Available Copies by Library ID")
      .get(s"/copies/library/$libraryId/available")
      .check(status.is(200))
  )

  val getCopiesByBookId = exec(
    http("Get Copies by Book ID")
      .get(s"/copies/book/$bookId")
      .check(status.is(200))
  )

  val getAvailableCopiesByBookId = exec(
    http("Get Available Copies by Book ID")
      .get(s"/copies/book/$bookId/available")
      .check(status.is(200))
  )

  val getAllAvailableCopies = exec(
    http("Get All Available Copies")
      .get("/copies/available")
      .check(status.is(200))
  )

  val scn = scenario("Copy Controller Endpoints")
    .exec(getCopyById)
    .pause(1)
    .exec(getAllCopies)
    .pause(1)
    .exec(getCopiesByLibraryId)
    .pause(1)
    .exec(getAvailableCopiesByBookIdInLibrary)
    .pause(1)
    .exec(getAvailableCopiesByLibraryId)
    .pause(1)
    .exec(getCopiesByBookId)
    .pause(1)
    .exec(getAvailableCopiesByBookId)
    .pause(1)
    .exec(getAllAvailableCopies)

  setUp(
    scn.inject(rampUsers(20).during(5.seconds))
  ).protocols(httpProtocol)
}
