package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.github.cdimascio.dotenv.Dotenv

class BookControllerSimulation extends Simulation {

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

  val bookId = "1"
  val title = "The Great Gatsby"
  val author = "F. Scott Fitzgerald"
  val isbn = "9780743273565"

  val getAllBooks = exec(
    http("Get All Books")
      .get("/books")
      .check(status.is(200))
  )

  val getBookById = exec(
    http("Get Book by ID")
      .get(s"/books/$bookId")
      .check(status.is(200))
  )

  val searchBooks = exec(
    http("Search Books")
      .get(s"/books/search?title=$title&author=$author&isbn=$isbn")
      .check(status.is(200))
  )

  val scn = scenario("Book Controller Endpoints")
    .exec(getAllBooks)
    .pause(1)
    .exec(getBookById)
    .pause(1)
    .exec(searchBooks)

  setUp(
    scn.inject(rampUsers(20).during(5.seconds))
  ).protocols(httpProtocol)
}
