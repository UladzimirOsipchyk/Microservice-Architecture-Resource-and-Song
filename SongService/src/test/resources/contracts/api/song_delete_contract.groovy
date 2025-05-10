package contracts.api

import org.springframework.cloud.contract.spec.Contract;


Contract.make {
  description("Should delete a song by IDs")

  request {
    method DELETE()
    url('/songs') {
      queryParameters {
        parameter 'ids': '1,2,3'
      }
    }
  }
  response {
    status OK()
    body(
        "ids":[1,2,3]
    )
  }
}