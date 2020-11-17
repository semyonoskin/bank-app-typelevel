package com.github.semyonoskin.pet.services

import cats.data.OptionT
import cats.effect.IO
import cats.syntax.flatMap._
import com.github.semyonoskin.pet.models.UserLogin
import com.github.semyonoskin.pet.repos.{AccountRepo, UserRepo}
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.syntax.applicativeError._
import com.github.semyonoskin.pet.repos.AccountRepo
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger


trait PaymentService {

  def transfer(sender: UserLogin, receiver: UserLogin, amount: Long): IO[Unit] // Payment service interface
}

object PaymentService {

  def make(users: UserRepo, accounts: AccountRepo, xa: Transactor[IO]): IO[PaymentService] =
    Slf4jLogger.create[IO].map { implicit l =>
      new Impl(users, accounts, xa)
    }

  final class Impl(users: UserRepo, accounts: AccountRepo, xa: Transactor[IO])(implicit logger: Logger[IO]) extends PaymentService {

    def transfer(sender: UserLogin, receiver: UserLogin, amount: Long): IO[Unit] = {
      val result = for {                                                        // Payment service implementation
        sender          <- OptionT(users.get(sender))
        receiver        <- OptionT(users.get(receiver))
        senderAccount   <- OptionT(accounts.get(sender.accountId))
        receiverAccount <- OptionT(accounts.get(receiver.accountId))
        } yield (senderAccount, receiverAccount)
      result.value.flatMap {
        case Some((senderAccount, receiverAccount)) if senderAccount.balance >= amount =>
           accounts.updateBalance(senderAccount.id, -amount) >> accounts.updateBalance(receiverAccount.id, amount)
        case Some((senderAccount, receiverAccount)) =>
          new Exception("Cannot satisfy transfer amount").raiseError[ConnectionIO, Unit]
        case None =>
          new Exception("No such users").raiseError[ConnectionIO, Unit]
      }.transact(xa) *> logger.info("transfered")


    }
  }

}