# Pollen Virtual Sensor

A SmartThings virtual device type handler (DTH) for retrieving pollen index data from [Pollen.com](https://www.pollen.com/).

## Setup

1. Install & publish the DTH into the **My Device Handlers** section of the SmartThings IDE via [GitHub integration](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html#setup) (recommended), or the via the old fashioned way of pasting the code into the **Create New Device Handler** > **From Code** window.

2. Go to the **My Devices** section of the IDE and click **New Device** to create a new device using the new DTH:
    1. **Name**: enter a readable name such as 'Pollen Index'
    2. **Device Network Id**: enter any short string of characters not already in use by another device.
    3. **Type**: pick **Pollen Virtual Sensor** from the dropdown (it will be near the bottom of the list).
    4. **Version**: Published
    5. **Location**: Pick your hub location from the dropdown.
    6. **Hub**: Pick your hub from the dropdown.
    7. Click **Create**.

3. Click **Save**. The virtual device is now ready to use.

## Use

Once setup is complete, you can view pollen index data via the SmartThings mobile app by checking the device under **My Home** > **Things**. The Things list will show the numeric pollen index value between 0 and 12. The device detail view will also show the category and the location for which pollen data is being reported.

The device handler will refresh data once an hour by default, but it does so via a somewhat unsupported method (SmartThings does not provide a supported method for devices to auto-refresh themselves). If it is not updating for you, use a device polling SmartApp such as [Pollster](https://github.com/statusbits/smartthings/blob/master/Pollster.md). The device will attempt to pull new data every time it is polled.

The data shown in the detail view is also accessible to SmartApps via device attributes. These can be used as inputs for other automations that support reading from devices using the generic **capability.sensor**, such as custom rules created in [WebCoRE](https://community.smartthings.com/t/faq-what-is-webcore-and-what-was-core/59981).

The attributes available from the device are:

| Attribute Name  | Format | Description  |
|---|---|---|
| index | Number | Pollen index for the configured location |
| category | Number | Category for the reported index number |
| triggers | String | Comma separated list of the top allergen triggers for the configured location |
| location | String | City or area name for the reported data, with 2-letter state code. May also contain basic error messages when data is unavailable from the API. |

You can view the current values for any of these on the **Recently** tab for the device in the SmartThings mobile app.
