package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv

class ReservationControllerSimulation extends Simulation {

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

  val libraryId = "3"
  val copyId = "3"
  val reservationId = "1"
  val userId = "21"

  val getAllReservations = exec(
    http("Get All Reservations")
      .get("/reservations")
      .check(status.is(200))
  )

  val getReservationById = exec(
    http("Get Reservation by ID")
      .get(s"/reservations/$reservationId")
      .check(status.is(200))
  )

  val getUserReservations = exec(
    http("Get User Reservations")
      .get(s"/reservations/user/$userId")
      .check(status.is(200))
  )

  val getMyActiveReservations = exec(
    http("Get My Active Reservations")
      .get("/reservations/me/active")
      .check(status.is(403))
  )

  val getLibraryReservations = exec(
    http("Get Library Reservations")
      .get(s"/reservations/library/$libraryId")
      .check(status.is(200))
  )

  val scn = scenario("Reservation Controller Endpoints")
    .exec(getAllReservations)
    .pause(1)
    .exec(getReservationById)
    .pause(1)
    .exec(getUserReservations)
    .pause(1)
    .exec(getMyActiveReservations)
    .pause(1)
    .exec(getLibraryReservations)

  setUp(
    scn.inject(rampUsers(20).during(5.seconds))
  ).protocols(httpProtocol)
}
