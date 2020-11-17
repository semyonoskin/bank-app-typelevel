package com.github.semyonoskin.pet.services

import cats.data.OptionT
import cats.effect.IO
import cats.syntax.flatMap._
import com.github.semyonoskin.pet.models.{Account, User, UserLogin}
import com.github.semyonoskin.pet.repos.{AccountRepo, UserRepo}
import doobie.util.transactor.Transactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.util.Random


trait UsersService {

  def createUser(login: UserLogin, balance: Long): IO[Unit]

  def getBalance(login: UserLogin): IO[Option[Long]] // User service interface

  def getUser(login: UserLogin): IO[Option[User]] // Concrete methods on Users, builded on base operations of User and Account repository
}


object UsersService {

  def make(users: UserRepo, accounts: AccountRepo, xa: Transactor[IO]): IO[UsersService] =
    Slf4jLogger.create[IO].map { implicit l =>
      new Impl(users, accounts, xa)
    }

  final class Impl(users: UserRepo, accounts: AccountRepo, xa: Transactor[IO])(implicit logger: Logger[IO]) extends UsersService {

    // User service implementation

    override def createUser(login: UserLogin, balance: Long): IO[Unit] =
      for {
        id <- IO(Random.nextLong())
        account = Account(id, balance)
        user = User(login, account.id)
        create = users.put(user) >> accounts.put(account)
        _ <- create.transact(xa)
        _ <- logger.info(s"Created user: $login, $balance")
      } yield ()

    override def getBalance(login: UserLogin): IO[Option[Long]] = {
      val balanceCIO =
        for {
          user <- OptionT(users.get(login))
          account <- OptionT(accounts.get(user.accountId))
        } yield account.balance
      balanceCIO.value.transact(xa).flatTap {
        case Some(balance) => logger.info(s"balance of $login: $balance")
        case None => IO.unit
      }
    }

    override def getUser(login: UserLogin): IO[Option[User]] =
      users.get(login).transact(xa).flatTap {
        case Some(user) => logger.info(s"$login")
        case None => IO.unit
      }
  }

}