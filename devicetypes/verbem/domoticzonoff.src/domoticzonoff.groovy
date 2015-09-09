/**
 *  Domoticz OnOff SubType Switch.
 *
 *  SmartDevice type for domoticz switches and dimmers.
 *  
 *
 *  Copyright (c) 2015 Martin Verbeek, based on X10 device from Geko
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Revision History
 *  ----------------
 *  2015-08-01
 */

metadata {
    definition (name:"domoticzOnOff", namespace:"verbem", author:"Martin Verbeek") {
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Polling"

        // custom attributes
        attribute "networkId", "string"
        attribute "deviceType", "string"

        // custom commands
        command "parse"     // (String "<attribute>:<value>[,<attribute>:<value>]")
       	command "setlevel"
    }

    tiles {
        standardTile("switch", "device.switch", width:2, height:2, canChangeIcon:true) {
		    state "off", label:'Off', icon:"st.switches.switch.off", backgroundColor:"#ffffff",
		        action:"switch.on" //, nextState:"on"
		    state "on", label:'On', icon:"st.switches.switch.on", backgroundColor:"#79b821",
		        action:"switch.off" //, nextState:"off"
        }
		
        controlTile("levelSliderControl", "device.level", "slider", height: 1,
          	width: 3, inactiveLabel: false, range:"(0..16)") {
            state "level", action:"setlevel"   
        }
        
        valueTile("networkId", "device.networkId", decoration:"flat", inactiveLabel:false) {
            state "default", label:'${currentValue}', inactiveLabel:false
        }

        standardTile("debug", "device.motion", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        
        details(["switch", "levelSliderControl", "networkId", "debug"])

        simulator {
            // status messages
            status "Switch On": "switch:1"
            status "Switch Off": "switch:0"
        }
    }
}

def parse(String message) {
    TRACE("parse(${message})")

    Map msg = stringToMap(message)
    if (msg?.size() == 0) {
        log.error "Invalid message: ${message}"
        return null
    }

    if (msg.containsKey("switch")) {
        def value = msg.switch.toInteger()
        switch (value) {
        case 0: off(); break
        case 1: on(); break
        }
    }

    STATE()
    return null
}

// switch.poll() command handler
def poll() {

    if (parent) {
        TRACE("poll() ${device.deviceNetworkId}")
        parent.domoticz_poll(getIDXAddress())
    }
}

// switch.poll() command handler
def refresh() {

    if (parent) {
        TRACE("refresh() ${device.deviceNetworkId}")
        parent.domoticz_poll(getIDXAddress())
    }
}

// switch.on() command handler
def on() {

    if (parent) {
        TRACE("on() ${device.deviceNetworkId}")
        parent.domoticz_on(getIDXAddress())
    }
}

// switch.off() command handler
def off() {

    if (parent) {
        TRACE("off() ${device.deviceNetworkId}")
        parent.domoticz_off(getIDXAddress())
    }
}

// Custom setlevel() command handler
def setlevel(level) {
    
    if (parent) {
        TRACE("setlevel()" + level)
        parent.domoticz_setlevel(getIDXAddress(), level)
    }
}

private def TRACE(message) {
    log.debug message
}

private def STATE() {
    log.debug "switch is ${device.currentValue("switch")}"
    log.debug "deviceNetworkId: ${device.deviceNetworkId}"
}

private String makeNetworkId(ipaddr, port) {

    String hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    String hexPort = String.format('%04X', port)
    return "${hexIp}:${hexPort}"
}

// gets the address of the device
private getHostAddress() {
	
    def ip = getDataValue("ip")
    def port = getDataValue("port")
    
    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    //log.debug "Using ip: $ip and port: $port for device: ${device.id}"
    return ip + ":" + port

}

// gets the IDX address of the device
private getIDXAddress() {
	
    def idx = getDataValue("idx")
        
    if (!idx) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            idx = parts[2]
        } else {
            log.warn "Can't figure out idx for device: ${device.id}"
        }
    }

    //log.debug "Using IDX: $idx for device: ${device.id}"
    return idx
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

/*----------------------------------------------------*/
/*			execute event can be called from the service manager!!!
/*----------------------------------------------------*/
def generateEvent (Map results) {
results.each { name, value ->
	log.info name + " " + value
	sendEvent(name:"${name}", value:"${value}")
    }
    return null
}