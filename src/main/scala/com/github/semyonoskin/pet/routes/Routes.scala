package com.github.semyonoskin.pet.routes

import cats.effect._
import com.github.semyonoskin.pet.models.UserLogin
import com.github.semyonoskin.pet.services.UsersService
import com.github.semyonoskin.pet.services.PaymentService
import com.github.semyonoskin.pet.services.UsersService
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.io._


object Routes {



  final case class CreateUserRequest(login: UserLogin, initBalance: Long)

  final case class TransferAmountRequest(sender: UserLogin, receiver: UserLogin, amount: Long)

  final case class UserAccount(login: UserLogin, balance: Long)



  final class Route(usersService: UsersService, paymentService: PaymentService) {


    implicit val userDecoder: EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]

    implicit val transferDecoder: EntityDecoder[IO, TransferAmountRequest] = jsonOf[IO, TransferAmountRequest]

    implicit val userAccDecoder: EntityDecoder[IO, UserAccount] = jsonOf[IO, UserAccount]


    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {


      case _ @ GET -> Root / "user"/ login/ "account" =>
        usersService.getBalance(login).flatMap{
          case Some(balance) => Ok(UserAccount(login, balance))
          case None => Ok("no such user")
        }


      case req @ POST -> Root / "create-user" =>
        for {
          user <- req.as[CreateUserRequest]
          resp <- Ok(usersService.createUser(user.login, user.initBalance))
        } yield resp


      case _ @ GET -> Root / "user"/ login  =>
        usersService.getUser(login).flatMap {
          case Some(user) => Ok(user)
          case None => Ok("not found")
        }


      case req @ POST -> Root / "transfer"  =>
        for {
          request <- req.as[TransferAmountRequest]
          resp <- Ok(paymentService.transfer(request.sender, request.receiver, request.amount))
        }yield resp


    }

  }

}
