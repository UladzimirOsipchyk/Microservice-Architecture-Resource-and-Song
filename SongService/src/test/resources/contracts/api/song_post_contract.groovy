package contracts.api

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
  description "Should create a new song"

  request {
    url "/songs"
    method POST()
    body(
        "name":"Bohemian Rhapsody",
        "artist":"Queen",
        "album":"A Night at the Opera",
        "duration":"15:20",
        "year":"1975"
    )
    headers {
      contentType(applicationJson())
    }
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body(
        "id":"1"
    )

  }

}