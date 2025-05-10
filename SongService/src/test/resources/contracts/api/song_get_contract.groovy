package contracts.api

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
  description "Should return song by id=1"

  request {
    url "/songs/1"
    method GET()
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body(
        "name":"Bohemian Rhapsody",
        "artist":"Queen",
        "album":"A Night at the Opera",
        "duration":"15:20",
        "year":"1975"
    )
  }

}
