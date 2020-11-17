package com.github.semyonoskin.pet.repos

import cats.implicits.toFunctorOps
import com.github.semyonoskin.pet.models.{User, UserLogin}
import com.github.semyonoskin.pet.models.User
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

trait UserRepo {

  def get(login: UserLogin): ConnectionIO[Option[User]] // User repository interface

  def put(user: User): ConnectionIO[Unit] // Base operations on Users
}


object UserRepo {

  def make: UserRepo = new DBImpl

  final class DBImpl extends UserRepo {

    override def get(login: UserLogin): ConnectionIO[Option[User]] =
      sql"SELECT * FROM Users WHERE login = $login".query[User].option

    override def put(user: User): ConnectionIO[Unit] =
      sql"INSERT INTO Users (login, accountid) VALUES (${user.login}, ${user.accountId})".update.run.void
  }
}