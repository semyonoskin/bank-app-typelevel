package com.github.semyonoskin.pet.models

import cats.effect.IO
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class User(login: UserLogin, accountId: Long)

object User {

  implicit val userdecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
}