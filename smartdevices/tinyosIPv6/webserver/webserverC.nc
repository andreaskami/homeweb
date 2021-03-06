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

#include <6lowpan.h>
#include <string.h>
#include "rest.h"

configuration webserverC {

} implementation {
  components MainC, LedsC;
  components webserverP;

  webserverP.Boot -> MainC;
  webserverP.Leds -> LedsC;

  components new TimerMilliC() as MulticastTimer;
  webserverP.MulticastTimer -> MulticastTimer;

  components IPDispatchC;
  webserverP.RadioControl -> IPDispatchC;

  components new UdpSocketC() as UdpSD;
  webserverP.UdpSD -> UdpSD;

  components new TcpSocketC() as TcpWeb, HttpdP;
  HttpdP.Boot -> MainC;
  HttpdP.Leds -> LedsC;
  HttpdP.Tcp -> TcpWeb;

  components new TimerMilliC() as SensingTimer;
  components new SensirionSht11C() as HumidTemp;
  components new HamamatsuS1087ParC() as TotalSolar;
  HttpdP.Temperature  -> HumidTemp.Temperature;
  HttpdP.Humidity     -> HumidTemp.Humidity;
  HttpdP.Illumination -> TotalSolar;
  HttpdP.SensingTimer -> SensingTimer;

}
