/**
 *  domoticzContact
 *
 *  Copyright 2016 Martin Verbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "domoticzContact", namespace: "verbem", author: "Martin Verbeek") {
		capability "Actuator"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Battery"
	}

preferences {
		input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}

tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "closed", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
				attributeState "off", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "Off", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "OFF", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "on", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
				attributeState "On", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
				attributeState "ON", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["contact", "temperature"])
		details(["contact","temperature","battery","refresh"])
	}

}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'contact' attribute

}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }

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

/*----------------------------------------------------*/
/*			execute event can be called from the service manager!!!
/*----------------------------------------------------*/
def generateEvent (Map results) {
    results.each { name, value ->
        log.info "generateEvent " + name + " " + value
        if (name == "switch") sendEvent(name:"contact", value:"${value}")
        else sendEvent(name:"${name}", value:"${value}")
        }
        return null
}