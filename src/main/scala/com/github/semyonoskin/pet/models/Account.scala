package com.github.semyonoskin.pet.models

import cats.effect.IO
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class Account(id: Long, balance: Long)

object Account {

  implicit val userdecoder: EntityDecoder[IO, Account] = jsonOf[IO, Account]
}