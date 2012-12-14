
module HttpdP {
  uses {
    interface Leds;
    interface Boot;
    interface Tcp;
    interface Read<uint16_t> as Temperature;
    interface Read<uint16_t> as Humidity;
    interface Read<uint16_t> as Illumination;
    interface Timer<TMilli> as SensingTimer;
  }
} implementation {

  static char *http_okay      = "HTTP/1.0 200 OK\r\n\r\n";
  static char *http_not_found = "HTTP/1.0 404 OK\r\n\r\n";
  static int http_okay_len = 19;

  uint16_t tempValue; 
  uint16_t humidValue;
  uint16_t illumValue;

  event void SensingTimer.fired(){
	call Temperature.read();
	call Humidity.read();
        call Illumination.read();
  }

  event void Temperature.readDone(error_t result, uint16_t data) {
	tempValue = data;
  }

  event void Humidity.readDone(error_t result, uint16_t data) {
	humidValue = data;
  }

  event void Illumination.readDone(error_t result, uint16_t data) {
	illumValue = data;
  }

  void process_request(int verb, char *request, int len) {
    char rep[24];

    switch(verb){
    	case HTTP_GET:

       		printfUART("request: '%s'\n", request);

       		if (strcmp(request,LIGHT) == 0){			// Light Resource
			char reply[24];
           		uint8_t bitmap = call Leds.get();
			memcpy(reply, "led0: 0 led1: 0 led2: 0\n", 24);
           		call Tcp.send(http_okay, http_okay_len);
           		if (bitmap & 1) reply[6] = '1';
           		if (bitmap & 2) reply[14] = '1';
           		if (bitmap & 4) reply[22] = '1';
			call Tcp.send(reply, 24);
           		call Tcp.send(request, 24);
			call Tcp.close();
      		}
       		else if (strcmp(request,TEMPERATURE) == 0){		// Temperature Resource
			int temperature = 0;
			char buffer[5];
			int i = 0;
			for(i=0; i<10; i++)
				buffer[i] = '\0';
			temperature = -40 + 0.01 * tempValue;
			itoa(temperature, buffer, 10);
        		call Tcp.send(http_okay, http_okay_len);
        		call Tcp.send(buffer, 5);
			call Tcp.close();
      		}
       		else if (strcmp(request,HUMIDITY) == 0){		// Humidity Resource
			int humidity = 0;
			char buffer[5];
			int i = 0;
			for(i=0; i<10; i++)
				buffer[i] = '\0';
			humidity = -4 + 0.0405 * humidValue + (-2.8 * 0.000001); 
			itoa(humidity, buffer, 10);
        		call Tcp.send(http_okay, http_okay_len);
        		call Tcp.send(buffer, 5);
			call Tcp.close();
      		}
       		else if (strcmp(request,ILLUMINATION) == 0){		// Illumination Resource
			int illumination = 0;
			char buffer[5];
			int i = 0;
			for(i=0; i<10; i++)
				buffer[i] = '\0';
			illumination = illumValue; 
			itoa(illumination, buffer, 10);
        		call Tcp.send(http_okay, http_okay_len);
        		call Tcp.send(buffer, 5);
			call Tcp.close();
      		}
       		else if (strcmp(request,ALIVENESS) == 0){		// Aliveness Resource
			char reply[1];
			memcpy(reply, "1", 1);
           		call Tcp.send(http_okay, http_okay_len);
           		call Tcp.send(reply, 1);
			call Tcp.close();
      		}
		else{							// not found
			call Tcp.send(http_not_found, http_okay_len);
			call Tcp.close();
		}

      		call Tcp.close();
		break;
	case HTTP_POST:
       		if (1){			// Light Resource
			char reply[24];
			memcpy(reply, "Leds changed OK", 15);
           		call Tcp.send(http_okay, http_okay_len);
			call Tcp.send(reply, 15);
           		call Tcp.send(request, 24);
			call Tcp.close();
      		}
		break;


	case HTTP_PUT:
		memcpy(rep, "PUT Resource OK\n", 18);
		call Tcp.send(http_okay, http_okay_len);
		call Tcp.send(rep, 24);
		call Tcp.close();
		break;

	case HTTP_DELETE:
		memcpy(rep, "DELETE Resource OK\n", 20);
		call Tcp.send(http_okay, http_okay_len);
		call Tcp.send(rep, 24);
		call Tcp.close();
		break;
   }
  }

  int http_state;
  int req_verb;
  char request_buf[150], *request;
  char tcp_buf[100];

  event void Boot.booted() {
    http_state = S_IDLE;
    call Tcp.bind(80);
    call SensingTimer.startPeriodic(5000);
  }

  event bool Tcp.accept(struct sockaddr_in6 *from, 
                            void **tx_buf, int *tx_buf_len) {
    if (http_state == S_IDLE) {
      http_state = S_CONNECTED;
      *tx_buf = tcp_buf;
      *tx_buf_len = 100;
      return TRUE;
    }
    printfUART("rejecting connection\n");
    return FALSE;
  }

  event void Tcp.connectDone(error_t e) {
    
  }

  event void Tcp.recv(void *payload, uint16_t len) {
    static int crlf_pos;
    char *msg = payload;
    switch (http_state) {
    case S_CONNECTED:
      crlf_pos = 0;
      request = request_buf;
      if (len < 3) {
        call Tcp.close();
        return;
      }
      if(msg[0] == 'G') {				// a GET command
        req_verb = HTTP_GET;
        msg += 3;
        len -= 3;
      }
      else if (msg[0] == 'P' && msg[1] == 'O') {	// a POST command
        req_verb = HTTP_POST;
        msg += 3;
        len -= 3;
      }
      else if (msg[0] == 'P' && msg[1] == 'U') {	// a PUT command
        req_verb = HTTP_PUT;
        msg += 3;
        len -= 3;
      }
      else if (msg[0] == 'D') {				// a DELETE command
        req_verb = HTTP_DELETE;
        msg += 3;
        len -= 3;
      }
      http_state = S_REQUEST_PRE;
    case S_REQUEST_PRE:
      while (len > 0 && *msg == ' ') {
        len--; msg++;
      }
      if (len == 0) break;
      http_state = S_REQUEST;
    case S_REQUEST:
      while (len > 0 && *msg != ' ') {
        *request++ = *msg++;
        len--;
      }
      if (len == 0) break;
      *request++ = '\0';
      http_state = S_HEADER;
    case S_HEADER:
      while (len > 0) {
        switch (crlf_pos) {
        case 0:
        case 2:
          if (*msg == '\r') crlf_pos ++;
          else if (*msg == '\n') crlf_pos += 2;
          else crlf_pos = 0;
          break;
        case 1:
        case 3:
          if (*msg == '\n') crlf_pos ++;
          else crlf_pos = 0;
          break;
        }
        len--; msg++;
        // if crlf == 2, we just finished a header line.  you know.  fyi.
        if (crlf_pos == 4) {
          http_state = S_BODY;
          process_request(req_verb, request_buf, request - request_buf - 1);
          break;
        } 
      }
    if (crlf_pos < 4) break;

    case S_BODY:
      // len might be zero here... just a note.
    default:
      call Tcp.close();
    }
  }

  event void Tcp.closed(error_t e) {
    call Leds.led2Toggle();

    call Tcp.bind(80);
    http_state = S_IDLE;
  }

  event void Tcp.acked() {

  }
}
