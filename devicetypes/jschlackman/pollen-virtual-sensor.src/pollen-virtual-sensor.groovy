/**
 *  Pollen Virtual Sensor
 *
 *  Author: jschlackman (james@schlackman.org)
 *  Version: 1.0
 *  Date: 2017-10-12
 *
 *  Latest version & ReadMe: https://github.com/jschlackman/PollenThing
 *
 */
metadata {
	definition (name: "Pollen Virtual Sensor", namespace: "jschlackman", author: "james@schlackman.org") {
		capability "Sensor"
		capability "Polling"

		attribute "index", "number"
		attribute "category", "string"
		attribute "triggers", "string"
		attribute "location", "string"

		command "refresh"
	}

	preferences {
		input name: "zipCode", type: "text", title: "Zip Code (optional)", required: false
		input name: "about", type: "paragraph", element: "paragraph", title: "Pollen Virtual Sensor 1.0", description: "By James Schlackman <james@schlackman.org>\n\nData source: Pollen.com"
	}

	tiles(scale: 2) {
		
		// Refresh button
		standardTile("refresh", "device.combined", width: 2, height: 2, decoration: "flat") {
			state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
		}

		// Pollen index tile for Things view
		standardTile("mainTile", "device.index", width: 2, height: 2, decoration: "flat", canChangeIcon: true) {
			state "default", label:'${currentValue}', icon: "st.Outdoor.outdoor23", // Defaults to grass icon from ST Outdoor category
				backgroundColors:[
					[value: 1.2, color: "#90d2a7"],
					[value: 3.6, color: "#44b621"],
					[value: 6, color: "#f1d801"],
					[value: 8.4, color: "#d04e00"],
					[value: 10.8, color: "#bc2323"],
				]
			}
			
		// Index category
		standardTile("category", "device.category", width: 4, height: 2, decoration: "flat") {
			state "default", label:'Category: ${currentValue}'
		}

		standardTile("index", "device.index", width: 2, height: 2, decoration: "flat") {
			state "default", label:'${currentValue}',
				backgroundColors:[
					[value: 1.2, color: "#90d2a7"],
					[value: 3.6, color: "#44b621"],
					[value: 6, color: "#f1d801"],
					[value: 8.4, color: "#d04e00"],
					[value: 10.8, color: "#bc2323"],
				]
			}


		// Top allergens
		standardTile("triggers", "device.triggers", width: 6, height: 2, decoration: "flat") {
			state "default", label:'Top Allergens: ${currentValue}'
		}

		// Location from API
		standardTile("location", "device.location", width: 4, height: 2, decoration: "flat") {
			state "default", label:'Location: ${currentValue}'
		}

		// Tile layout for main listing and details screen
		main("mainTile")
		details("category", "index", "triggers", "location", "refresh")
	}
}

// Parse events into attributes. This will never be called but needs to be present in the DTH code.
def parse(String description) {
	log.debug("Pollen Sensor: Parsing '${description}'")
}

def installed() {
	runEvery1Hour(poll)
	poll()
}

def updated() {
	poll()
}

def uninstalled() {
	unschedule()
}

// handle commands
def poll() {
	def pollenZip = null

	// Use hub zipcode if user has not defined their own
	if(zipCode) {
		pollenZip = zipCode
	} else {
		pollenZip = location.zipCode
	}
	
	log.debug("Getting pollen data for ZIP: ${pollenZip}")

	// Set up the Pollen.com API query
	def params = [
		uri: 'https://www.pollen.com/api/forecast/current/pollen/',
		path: pollenZip,
		headers: [Referer:'https://www.pollen.com']
	]

	try {
	// Send query to the Pollen.com API
		httpGet(params) {resp ->

		// Parse the periods data array
			resp.data.Location.periods.each {period ->
				
				// Only interested in today's forecast
				if (period.Type == 'Today') {
					
					// Pollen index
					send(name: "index", value: period.Index)
					
					def catName = ""
					def indexNum = period.Index.toFloat()
					
					// Set the category according to index thresholds
					if (indexNum < 2.5) {catName = "Low"}
					else if (indexNum < 4.9) {catName = "Low-Medium"}
					else if (indexNum < 7.3) {catName = "Medium"}
					else if (indexNum < 9.7) {catName = "Medium-High"}
					else if (indexNum < 12) {catName = "High"}
					else {catName = "Unknown"}
				
					send(name: "category", value: catName)
					
					// Build the list of allergen triggers
					def triggersList = period.Triggers.inject([]) { result, entry ->
						result << "${entry.Name}"
					}.join(", ")
					
					send(name: "triggers", value: triggersList)
				}

				// Forecast location
				send(name: "location", value: resp.data.Location.DisplayLocation)

			}
		
		}

	}
	catch (SocketTimeoutException e) {
		log.error("Connection to Pollen.com API timed out.")
		send(name: "location", value: "Connection timed out while retrieving data from API")
	}
	catch (e) {
		log.error("Could not retrieve pollen data: $e")
		send(name: "location", value: "Could not retrieve data from API")
	}

}

def refresh() {
	poll()
}

def configure() {
	poll()
}

private send(map) {
	//log.debug "Pollen: event: $map"
	sendEvent(map)
}