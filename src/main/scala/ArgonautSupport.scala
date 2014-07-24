/*
 * Copyright 2014 Pellucid Analytics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pellucid.argonautspray

import argonaut._, Argonaut._

import spray.http.{ ContentTypes, ContentTypeRange, HttpCharsets, HttpEntity, MediaTypes }
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.{ Deserialized, MalformedContent, SimpleUnmarshaller, Unmarshaller }


/** A trait providing automatic to and from JSON marshalling/unmarshalling
  * using in-scope ''argonaut'' EncodeJson/DecodeJson.
  * Note that ''argonaut-spray'' does not have an automatic dependency on
  * ''argonaut'', ''spray'', or ''akka''.
  * You'll need to provide the appropriate artifacts yourself.
  */
trait ArgonautSupport {

  /** marshall from an Argonaut Json value */
  implicit def argonautMarshallerFromJson(
      implicit prettyPrinter: PrettyParams = PrettyParams.nospace
  ): Marshaller[Json] =
    Marshaller.delegate[Json, String](ContentTypes.`application/json`)(jsValue =>
      prettyPrinter.pretty(jsValue)
    )

  /** unmarshall to an Argonaut Json value */
  implicit val argonautUnmarshallerToJson: Unmarshaller[Json] =
    delegate[String, Json](MediaTypes.`application/json`)(string =>
      JsonParser.parse(string).toEither.left.map(e => MalformedContent(e))
    )(UTF8StringUnmarshaller)

  /** marshall from a T that can be encoded to an Argonaut Json value */
  implicit def argonautMarshallerFromT[T](
      implicit encodeJson: EncodeJson[T],
               prettyPrinter: PrettyParams = PrettyParams.nospace
  ): Marshaller[T] =
    Marshaller.delegate[T, String](ContentTypes.`application/json`)(value =>
      prettyPrinter.pretty(encodeJson.encode(value))
    )

  /** unmarshall to a T that can be decoded from an Argonaut Json value */
  implicit def argonautUnmarshallerToT[T : DecodeJson]: Unmarshaller[T] =
    delegate[String, T](MediaTypes.`application/json`)(string =>
      string.decodeEither[T].toEither.left.map(e => MalformedContent(e))
    )(UTF8StringUnmarshaller)

  private val UTF8StringUnmarshaller = new Unmarshaller[String] {
    def apply(entity: HttpEntity) = Right(entity.asString(defaultCharset = HttpCharsets.`UTF-8`))
  }

  // Unmarshaller.delegate is used as a kind of map operation; play-json JsResult can contain either validation errors or the JsValue
  // representing a JSON object. We need a delegate method that works as a flatMap and let the provided A => Deserialized[B] function
  // to deal with any possible error, including exceptions.
  //
  private def delegate[A, B](unmarshalFrom: ContentTypeRange*)(f: A => Deserialized[B])(implicit ma: Unmarshaller[A]): Unmarshaller[B] =
    new SimpleUnmarshaller[B] {
      val canUnmarshalFrom = unmarshalFrom
      def unmarshal(entity: HttpEntity) = ma(entity).right.flatMap(a => f(a))
    }
}

object ArgonautSupport extends ArgonautSupport
