/*
 * "Copyright (c) 2008 The Regents of the University  of California.
 * All rights reserved."
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 */

#include <IPDispatch.h>
#include <lib6lowpan.h>
#include <ip.h>
#include <lib6lowpan.h>
#include <ip.h>

#include "PrintfUART.h"

module webserverP {
  uses {
    interface Boot;
    interface SplitControl as RadioControl;
    interface Leds;
    interface Timer<TMilli> as MulticastTimer;
    interface UDP as UdpSD;
  }

} implementation {

  bool timerStarted;
  uint16_t discoveryTimer = 0;
  struct sockaddr_in6 route_dest;

  #ifndef SIM
  #define CHECK_NODE_ID
  #else
  #define CHECK_NODE_ID if (TOS_NODE_ID == BASESTATION_ID) return
  #endif

  event void Boot.booted() {
    CHECK_NODE_ID;
    call RadioControl.start();
    timerStarted = FALSE;
    printfUART_init();
    
    // start the service description multicasting service, every 20 seconds
    call MulticastTimer.startPeriodic(1024*20);
    
    dbg("Boot", "booted: %i\n", TOS_NODE_ID);
  }

  event void RadioControl.startDone(error_t e) {

  }

  event void RadioControl.stopDone(error_t e) {

  }

  /* it sends a multicasting service description message, every x seconds */
  event void MulticastTimer.fired() {
	
   // prepare a service description message
   struct sockaddr_in6 sa6;

   discoveryTimer++;

   if(discoveryTimer == 2){
	call MulticastTimer.stop();
   }
   else{
   	inet_pton6("ff02::1", &sa6.sin6_addr);
   	sa6.sin6_port = htons(10000);
   	call UdpSD.sendto(&sa6, device_desc, strlen(device_desc));	
        
   	if(timerStarted == FALSE){
		call Leds.led1On();
		timerStarted = TRUE;
   	}
   	else{
		call Leds.led1Off();
		timerStarted = FALSE;
   	}
   }
  }

  event void UdpSD.recvfrom(struct sockaddr_in6 *from, void *data,
                           uint16_t len, struct ip_metadata *meta) {
  }


}
