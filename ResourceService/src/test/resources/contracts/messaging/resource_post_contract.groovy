package contracts.messaging

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
  name("resourceProcessCreatedEvent")
  description("Should send resource processing message for uploaded event")

  label("resource.processing.requested")

  input {
    triggeredBy("triggerResourceDeletedEvent()")
  }

  outputMessage {
    sentTo("ResourceQueue")
    headers {
      header("contentType", textPlain())
    }
    body(
        id     : "1",
        key    : "sample.mp3",
        action : "extract_meta"
    )
  }
}