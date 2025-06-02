package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv

class LoanControllerSimulation extends Simulation {

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

  val loanId = "1"
  val userId = "24"
  val libraryId = "2"

  val getAllLoans = exec(
    http("Get All Loans")
      .get("/loans")
      .check(status.is(200))
  )

  val getLoanById = exec(
    http("Get Loan by ID")
      .get(s"/loans/$loanId")
      .check(status.is(200))
  )

  val getUserLoans = exec(
    http("Get Loans by User ID")
      .get(s"/loans/user/$userId")
      .check(status.is(200))
  )

  val getMyLoans = exec(
    http("Get My Loans")
      .get("/loans/me")
      .check(status.is(403))
  )

  val getMyActiveLoans = exec(
    http("Get My Active Loans")
      .get("/loans/me/active")
      .check(status.is(403))
  )

  val getLibraryLoans = exec(
    http("Get Library Loans")
      .get(s"/loans/library/$libraryId")
      .check(status.in(200))
  )

  val scn = scenario("Loan Controller Endpoints")
    .exec(getAllLoans)
    .pause(1)
    .exec(getLoanById)
    .pause(1)
    .exec(getUserLoans)
    .pause(1)
    .exec(getMyLoans)
    .pause(1)
    .exec(getMyActiveLoans)
    .pause(1)
    .exec(getLibraryLoans)

  setUp(
    scn.inject(rampUsers(20).during(5.seconds))
  ).protocols(httpProtocol)
}
