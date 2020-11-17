package com.github.semyonoskin.pet

import com.github.semyonoskin.pet.routes.Routes.Route
import cats.effect._
import org.http4s.implicits._
import org.http4s.server.blaze._
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext.global
import cats.effect.IOApp
import com.github.semyonoskin.pet.repos.{AccountRepo, UserRepo}
import com.github.semyonoskin.pet.services.{PaymentService, UsersService}
import doobie.util.ExecutionContexts
import org.http4s.server.Router


object App extends IOApp {

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql://localhost:5432/test", // connect URL (driver-specific)
    "test_admin", // user
    "1234", // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  val userRepo: UserRepo = UserRepo.make
  val accountRepo: AccountRepo = AccountRepo.make

  def run(args: List[String]): IO[ExitCode] =
    init.flatMap { app =>
      BlazeServerBuilder[IO](global)
        .bindHttp(8000, "localhost")
        .withHttpApp(app)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }

  private def init =
    for {
      paymentService <- PaymentService.make(userRepo, accountRepo, transactor)
      createUserService <- UsersService.make(userRepo, accountRepo, transactor)
      h = new Route(createUserService, paymentService)
      app = Router("/" -> h.routes).orNotFound
    } yield app
}
