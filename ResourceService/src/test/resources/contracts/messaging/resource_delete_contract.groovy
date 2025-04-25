package contracts.messaging

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
  name("resourceProcessDeletedEvent")
  description("Should send resource processing message for delete event")

  label("resource.processing.requested")

  input {
    triggeredBy("triggerResourceCreatedEvent()")
  }

  outputMessage {
    sentTo("ResourceQueue")
    headers {
      header("contentType", textPlain())
    }
    body(
        id     : "1,2,3",
        key    : "null",
        action : "remove_meta"
    )
  }
}