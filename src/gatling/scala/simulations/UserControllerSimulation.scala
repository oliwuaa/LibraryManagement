package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv
import java.util.UUID

class UserControllerSimulation extends Simulation {

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

  val email = "john.doe@example.com"
  val userId = "21"
  val libraryId = "3"

  val getAllUsers = exec(http("Get All Users").get("/users"))

  val getMe = exec(http("Get Me").get("/users/me"))

  val fetchUserId = exec(
    http("Get User ID by Email")
      .get(s"/users/search?email=$email")
      .check(jsonPath("$[0].id").saveAs("userId"))
  )

  val getUserById = exec(
    http("Get User by ID")
      .get(s"/users/$userId")
      .check(status.is(200))
  )

  val getLibrariansFromLibrary = exec(
    http("Get Librarians From Library")
      .get(s"/users/library/$libraryId/librarians")
      .check(status.is(200))
  )

  val searchLibrarians = exec(
    http("Search Librarians")
      .get("/users/search?role=LIBRARIAN")
      .check(status.is(200))
  )

  val searchUsers = exec(
    http("Search Users")
      .get("/users/search?role=USER")
      .check(status.is(200))
  )

  val scn = scenario("User Controller Endpoints")
    .exec(getMe)
    .pause(1)
    .exec(fetchUserId)
    .pause(1)
    .exec(getUserById)
    .pause(1)
    .exec(getAllUsers)
    .pause(1)
    .exec(searchLibrarians)
    .pause(1)
    .exec(searchUsers)
    .pause(1)
    .exec(getLibrariansFromLibrary)

  setUp(
    scn.inject(rampUsers(20).during(10.seconds))
  ).protocols(httpProtocol)
}
