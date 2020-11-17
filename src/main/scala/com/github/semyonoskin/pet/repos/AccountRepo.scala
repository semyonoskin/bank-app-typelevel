package com.github.semyonoskin.pet.repos

import com.github.semyonoskin.pet.models.Account
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

trait AccountRepo {

  def get(id: Long): ConnectionIO[Option[Account]]

  def updateBalance(id: Long, amount: Long): ConnectionIO[Unit] // Accounts repository interface

  def put(account: Account): ConnectionIO[Unit]                 // Base operations on accounts
}


object AccountRepo {

  def make: AccountRepo = new DBImpl

  final class DBImpl extends AccountRepo {


    def get(id: Long): ConnectionIO[Option[Account]] =
      sql"SELECT * FROM Accounts WHERE id = $id".query[Account].option

    def updateBalance(id: Long, amount: Long): ConnectionIO[Unit] =
      sql"UPDATE Accounts SET balance = balance + $amount WHERE id = $id".update.run.void

    def put(account: Account): ConnectionIO[Unit] =
      sql"INSERT INTO Accounts (id, balance) VALUES (${account.id}, ${account.balance})".update.run.void
  }

}