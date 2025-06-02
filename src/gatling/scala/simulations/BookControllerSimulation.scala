package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BookControllerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val login = exec(
    http("Login")
      .post("/auth/login")
      .body(StringBody("""{ "email": "user@example.com", "password": "user" }""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.accessToken").saveAs("jwt"))
  ).exec { session =>
    println("JWT token: " + session("jwt").asOption[String].getOrElse("NO TOKEN"))
    session
  }

  val getAllBooks = exec(
    http("Get All Books")
      .get("/books")
      .header("Authorization", session => "Bearer " + session("jwt").as[String])
      .check(status.is(200))
  )

  val getBookById = exec(
    http("Get Book by ID")
      .get("/books/1")
      .header("Authorization", session => "Bearer " + session("jwt").as[String])
      .check(status.is(200))
  )

  val searchBooks = exec(
    http("Search Books")
      .get("/books/search?title=The Great Gatsby&author=F. Scott Fitzgerald&isbn=9780743273565")
      .header("Authorization", session => "Bearer " + session("jwt").as[String])
      .check(status.is(200))
  )

  val scn = scenario("Book Controller Endpoints (Authenticated)")
    .exec(login)
    .pause(1)
    .exec(getAllBooks)
    .pause(1)
    .exec(getBookById)
    .pause(1)
    .exec(searchBooks)

  setUp(
    scn.inject(rampUsers(9).during(10.seconds))
  ).protocols(httpProtocol)
}