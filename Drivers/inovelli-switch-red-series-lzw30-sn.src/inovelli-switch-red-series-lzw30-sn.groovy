/**
 *  Inovelli Switch Red Series LZW30-SN (segv11-additions)
 *  Author: Eric Maycock (erocm123)
 *  Date: 2022-02-03
 *
 *  Copyright 2022 Eric Maycock / Inovelli
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
 *  2022-02-03: Fixing bug when LED color gets stuck on "custom value".
 *
 *  2021-11-30: Adding push & hold methods for Hubitat capability updates.
 *  
 *  2021-11-02: Fix and add support for Hubitat's change in componentSetColorTemperature (now supports level).
 *  
 *  2021-05-25: Updating method that is used to determine whether to send non-secure, S0, or S2. 
 *  
 *  2021-05-10: Adding "LED When Off" child device option. 
 *  
 *  2021-04-09: Fix digital command methods "holdUp, holdDown, etc." for those that use them. 
 *
 *  2021-03-10: Adding parameter numbers to preferences description.  
 *
 *  2020-10-01: Adding custom command setConfigParameter(number, value, size) to be able to easily
 *              set parameters from Rule Machine.  
 *
 *  2020-08-27: Cleaning up device fingerprint info. 
 *
 *  2020-08-25: Fix for button events not getting sent correctly on C7. 
 *              Adding componentSetColorTemperature to allow LED child device to change LED color to white.
 *              If you set the color of child device it changes it to the specified RGB color. If you
 *              set the color temperature it will change the LED to white.
 *              If you update the driver for an already included device you will need to change the
 *              child device driver to Generic Component RGBW.
 *
 *  2020-08-07: Fix for when setting the LED color via drop down to white the custom color field 
 *              gets populated without user realizing it. 
 *
 *  2020-08-05: Adding S2 support for C-7 Hub. 
 *
 *  2020-07-06: Added a configuration parameter (51) that allows you to disable the 700ms delay when turing switch on/off from the wall.
 *              Also adding white LED option to LED colors. Both of these require firmware 1.19+
 *
 *  2020-06-02: Change setColor to leave indicator level alone if level is not specified with command. 
 *              LED Indicator child device now works with setLevel as well as setColor.
 *
 *  2020-05-19: Adding setIndicator command to match Hubitat built in driver. 
 *
 *  2020-05-13: Removed ColorControl capability as it was conflicting with some in-built Hubitat apps.
 *              Added LED Color child device that can be used in its place.  
 *
 *  2020-05-05: Adding ColorControl capability to allow changing the LED bar color easily with setColor.
 *              Adding preferences to automatically disable logs after x minutes. Previously the informational
 *              logging would disable after 30 minutes without an option for the user.
 *
 *  2020-05-01: Correctly distinguish between digital and physical on / off.
 *
 *  2020-03-27: Adding additional duration options for Notifications. Also adding the commands  
 *              startNotification(value) and stopNotification() to be used in apps like
 *              WebCoRE or Rule Machine to directly control notifications instead of through child Devices.
 *              To determine "value": https://nathanfiscus.github.io/inovelli-notification-calc/
 *
 *  2020-02-25: Switch over to using Hubitat child device drivers. Should still be backwards compatible with
 *              Inovelli child drivers.
 * 
 *  2020-02-07: Update preferences when user changes parameter or disables relay from switch or from child device.
 *
 *  2020-02-06: Fix for remote control child device being created when it shouldn't be.
 *              Fix for local protection being updated via hub after being changed with config button.
 *
 *  2020-02-05: Fix for LED turning off after 3 seconds when LED intensity (when off) is set to 0.
 *              Extra button event added for those that want to distinguish held vs pushed. 
 *              Button 8 pushed = Up button held. Button 8 held = Down button held.
 *              Button 6 pushed = Up button released. Button 6 pushed = Down button released. 
 *
 *  2020-01-28: Update VersionReport parsing because of Hubitat change. Removing unnecessary reports.
 *
 *  2019-12-03: Specify central scene command class version for upcoming Hubitat update.
 *
 *  2019-11-20: Fixed Association Group management.
 *
 *  2019-10-15: Ability to create child devices for local & rf protection to use in various automations.
 *              Device label is now displayed in logging. 
 * 
 *  2019-10-01: Adding the ability to set a custom color for the RGB indicator. Use a hue 360 color wheel.
 *              Adding the ability to enable z-wave "rf protection" to disable control from z-wave commands.
 *
 */

import groovy.transform.Field
import groovy.json.JsonOutput

@Field static List ledNotificationEndpoints = [8]
@Field static Map ledColorEndpoints = [103:5]
@Field static Map ledIntensityEndpoints = [103:6]
@Field static Map ledIntensityOffEndpoints = [104:7]
 
metadata {
    definition (name: "Inovelli Switch Red Series LZW30-SN (segv11)", namespace: "InovelliUSA", author: "Eric Maycock", vid: "generic-switch", importUrl: "https://raw.githubusercontent.com/segv11/Inovelli-Hubitat/segv11-additions/Drivers/inovelli-switch-red-series-lzw30-sn.src/inovelli-switch-red-series-lzw30-sn.groovy") {
        capability "Switch"
        capability "Refresh"
        capability "Actuator"
        capability "Sensor"
        capability "PushableButton"
        capability "HoldableButton"
        capability "Configuration"
        capability "Energy Meter"
        capability "Power Meter"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        attribute "groups", "Number"
        /*** segv11 meterExtra */
        attribute "energyTime" , "Number"
        attribute "energyDuration" , "String"
        attribute "powerHigh", "Number"
        attribute "powerLow", "Number"
        /* segv11 meterExtra ***/

        
        // Uncomment these lines if you would like to test your scenes with digital button presses.
        /*
        command "pressUpX1"
        command "pressDownX1"
        command "pressUpX2"
        command "pressDownX2"
        command "pressUpX3"
        command "pressDownX3"
        command "pressUpX4"
        command "pressDownX4"
        command "pressUpX5"
        command "pressDownX5"
        command "holdUp"
        command "holdDown"
        command "releaseUp"
        command "releaseDown"
        command "pressConfig"
        */
        
        command "childOn", ["string"]
        command "childOff", ["string"]
        command "childRefresh", ["string"]
        command "componentOn", ["string"]
        command "componentOff", ["string"]
        command "componentSetLevel", ["string", "number"]
        command "componentRefresh", ["string"]
        command "componentSetColor", ["string"]
        command "componentSetColorTemperature"
        command "setIndicator", [[name: "Set Indicator*",type:"NUMBER", description: "For configuration values see: https://nathanfiscus.github.io/inovelli-notification-calc/"]]
        
        command "reset", []
        
        command "startNotification",   [[name: "Start Notification*",type:"NUMBER", description: "For configuration values see: https://nathanfiscus.github.io/inovelli-notification-calc/"],
                                        [name: "Endpoint",type:"NUMBER", description: "Optional. Only used on devices with multiple indicator bars."]]
        
        command "stopNotification",    [[name: "Endpoint",type:"NUMBER", description: "Optional. Only used on devices with multiple indicator bars."]]
        
        command "setAssociationGroup", [[name: "Group Number*",type:"NUMBER", description: "Provide the association group number to edit"], 
                                        [name: "Z-Wave Node*", type:"STRING", description: "Enter the node number (in hex) associated with the node"], 
                                        [name: "Action*", type:"ENUM", constraints: ["Add", "Remove"]],
                                        [name:"Multi-channel Endpoint", type:"NUMBER", description: "Currently not implemented"]] 
        
        command "setConfigParameter",  [[name: "Number*",type:"NUMBER", description: "Provide the parameter number to edit"], 
                                        [name: "Value*", type:"NUMBER", description: "Enter the value you would like to set the parameter to"], 
                                        [name: "Size*", type:"ENUM", constraints: ["1", "2", "4"]]]

        fingerprint mfr: "031E", prod: "0002", deviceId: "0001", inClusters:"0x5E,0x70,0x85,0x59,0x55,0x86,0x72,0x5A,0x73,0x32,0x5B,0x98,0x9F,0x25,0x6C,0x75,0x22,0x7A"
    }

    simulator {
    }
    
    preferences {
        generate_preferences()
    }
}

def generate_preferences()
{
    getParameterNumbers().each { i ->
        
        switch(getParameterInfo(i, "type"))
        {   
            case "number":
                input "parameter${i}", "number",
                    title:"${i}. " + getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description") + "\nRange: " + getParameterInfo(i, "options") + "\nDefault: " + getParameterInfo(i, "default"),
                    range: getParameterInfo(i, "options")
                    //defaultValue: getParameterInfo(i, "default")
            break
            case "enum":
                input "parameter${i}", "enum",
                    title:"${i}. " + getParameterInfo(i, "name") + "\n" + getParameterInfo(i, "description"), 
                    //defaultValue: getParameterInfo(i, "default"),
                    options: getParameterInfo(i, "options")
            break
        }
        if (i == 5){
           input "parameter5custom", "number", 
               title: "Custom LED RGB Value", 
               description: "\nInput a custom value in this field to override the above setting. The value should be between 0 - 360 and can be determined by using the typical hue color wheel.", 
               required: false,
               range: "0..360"
        }
    }
    
    input description: "When each notification set (Color, Level, Duration, Type) is configured, a switch child device is created that can be used in Apps to activate that notification.", title: "LED Notifications", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    
    [1,2,3,4,5].each { i ->
                input "parameter8-${i}a", "enum", title: "LED Effect Color - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Red",
                    21:"Orange",
                    42:"Yellow",
                    85:"Green",
                    127:"Cyan",
                    170:"Blue",
                    212:"Violet",
                    234:"Pink",
                    255:"White (Firmware 1.19+)"]
                input "parameter8-${i}b", "enum", title: "LED Effect Level - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"0%",
                    1:"10%",
                    2:"20%",
                    3:"30%",
                    4:"40%",
                    5:"50%",
                    6:"60%",
                    7:"70%",
                    8:"80%",
                    9:"90%",
                    10:"100%"]
                input "parameter8-${i}c", "enum", title: "LED Effect Duration - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    255:"Indefinitely",
                    1:"1 Second",
                    2:"2 Seconds",
                    3:"3 Seconds",
                    4:"4 Seconds",
                    5:"5 Seconds",
                    6:"6 Seconds",
                    7:"7 Seconds",
                    8:"8 Seconds",
                    9:"9 Seconds",
                    10:"10 Seconds",
                    11:"11 Seconds",
                    12:"12 Seconds",
                    13:"13 Seconds",
                    14:"14 Seconds",
                    15:"15 Seconds",
                    16:"16 Seconds",
                    17:"17 Seconds",
                    18:"18 Seconds",
                    19:"19 Seconds",
                    20:"20 Seconds",
                    21:"21 Seconds",
                    22:"22 Seconds",
                    23:"23 Seconds",
                    24:"24 Seconds",
                    25:"25 Seconds",
                    26:"26 Seconds",
                    27:"27 Seconds",
                    28:"28 Seconds",
                    29:"29 Seconds",
                    30:"30 Seconds",
                    31:"31 Seconds",
                    32:"32 Seconds",
                    33:"33 Seconds",
                    34:"34 Seconds",
                    35:"35 Seconds",
                    36:"36 Seconds",
                    37:"37 Seconds",
                    38:"38 Seconds",
                    39:"39 Seconds",
                    40:"40 Seconds",
                    41:"41 Seconds",
                    42:"42 Seconds",
                    43:"43 Seconds",
                    44:"44 Seconds",
                    45:"45 Seconds",
                    46:"46 Seconds",
                    47:"47 Seconds",
                    48:"48 Seconds",
                    49:"49 Seconds",
                    50:"50 Seconds",
                    51:"51 Seconds",
                    52:"52 Seconds",
                    53:"53 Seconds",
                    54:"54 Seconds",
                    55:"55 Seconds",
                    56:"56 Seconds",
                    57:"57 Seconds",
                    58:"58 Seconds",
                    59:"59 Seconds",
                    61:"1 Minute",
                    62:"2 Minutes",
                    63:"3 Minutes",
                    64:"4 Minutes",
                    65:"5 Minutes",
                    66:"6 Minutes",
                    67:"7 Minutes",
                    68:"8 Minutes",
                    69:"9 Minutes",
                    70:"10 Minutes",
                    71:"11 Minutes",
                    72:"12 Minutes",
                    73:"13 Minutes",
                    74:"14 Minutes",
                    75:"15 Minutes",
                    76:"16 Minutes",
                    77:"17 Minutes",
                    78:"18 Minutes",
                    79:"19 Minutes",
                    80:"20 Minutes",
                    81:"21 Minutes",
                    82:"22 Minutes",
                    83:"23 Minutes",
                    84:"24 Minutes",
                    85:"25 Minutes",
                    86:"26 Minutes",
                    87:"27 Minutes",
                    88:"28 Minutes",
                    89:"29 Minutes",
                    90:"30 Minutes",
                    91:"31 Minutes",
                    92:"32 Minutes",
                    93:"33 Minutes",
                    94:"34 Minutes",
                    95:"35 Minutes",
                    96:"36 Minutes",
                    97:"37 Minutes",
                    98:"38 Minutes",
                    99:"39 Minutes",
                    100:"40 Minutes",
                    101:"41 Minutes",
                    102:"42 Minutes",
                    103:"43 Minutes",
                    104:"44 Minutes",
                    105:"45 Minutes",
                    106:"46 Minutes",
                    107:"47 Minutes",
                    108:"48 Minutes",
                    109:"49 Minutes",
                    110:"50 Minutes",
                    111:"51 Minutes",
                    112:"52 Minutes",
                    113:"53 Minutes",
                    114:"54 Minutes",
                    115:"55 Minutes",
                    116:"56 Minutes",
                    117:"57 Minutes",
                    118:"58 Minutes",
                    119:"59 Minutes",
                    121:"1 Hour",
                    122:"2 Hours",
                    123:"3 Hours",
                    124:"4 Hours",
                    125:"5 Hours",
                    126:"6 Hours",
                    127:"7 Hours",
                    128:"8 Hours",
                    129:"9 Hours",
                    130:"10 Hours",
                    131:"11 Hours",
                    132:"12 Hours",
                    133:"13 Hours",
                    134:"14 Hours",
                    135:"15 Hours",
                    136:"16 Hours",
                    137:"17 Hours",
                    138:"18 Hours",
                    139:"19 Hours",
                    140:"20 Hours",
                    141:"21 Hours",
                    142:"22 Hours",
                    143:"23 Hours",
                    144:"24 Hours",
                    145:"25 Hours",
                    146:"26 Hours",
                    147:"27 Hours",
                    148:"28 Hours",
                    149:"29 Hours",
                    150:"30 Hours",
                    151:"31 Hours",
                    152:"32 Hours",
                    153:"33 Hours",
                    154:"34 Hours",
                    155:"35 Hours",
                    156:"36 Hours",
                    157:"37 Hours",
                    158:"38 Hours",
                    159:"39 Hours",
                    160:"40 Hours",
                    161:"41 Hours",
                    162:"42 Hours",
                    163:"43 Hours",
                    164:"44 Hours",
                    165:"45 Hours",
                    166:"46 Hours",
                    167:"47 Hours",
                    168:"48 Hours",
                    169:"49 Hours",
                    170:"50 Hours",
                    171:"51 Hours",
                    172:"52 Hours",
                    173:"53 Hours",
                    174:"54 Hours",
                    175:"55 Hours",
                    176:"56 Hours",
                    177:"57 Hours",
                    178:"58 Hours",
                    179:"59 Hours",
                    180:"60 Hours",
                    181:"61 Hours",
                    182:"62 Hours",
                    183:"63 Hours",
                    184:"64 Hours",
                    185:"65 Hours",
                    186:"66 Hours",
                    187:"67 Hours",
                    188:"68 Hours",
                    189:"69 Hours",
                    190:"70 Hours",
                    191:"71 Hours",
                    192:"72 Hours",
                    193:"73 Hours",
                    194:"74 Hours",
                    195:"75 Hours",
                    196:"76 Hours",
                    197:"77 Hours",
                    198:"78 Hours",
                    199:"79 Hours",
                    200:"80 Hours",
                    201:"81 Hours",
                    202:"82 Hours",
                    203:"83 Hours",
                    204:"84 Hours",
                    205:"85 Hours",
                    206:"86 Hours",
                    207:"87 Hours",
                    208:"88 Hours",
                    209:"89 Hours",
                    210:"90 Hours",
                    211:"91 Hours",
                    212:"92 Hours",
                    213:"93 Hours",
                    214:"94 Hours",
                    215:"95 Hours",
                    216:"96 Hours",
                    217:"97 Hours",
                    218:"98 Hours",
                    219:"99 Hours",
                    220:"100 Hours",
                    221:"101 Hours",
                    222:"102 Hours",
                    223:"103 Hours",
                    224:"104 Hours",
                    225:"105 Hours",
                    226:"106 Hours",
                    227:"107 Hours",
                    228:"108 Hours",
                    229:"109 Hours",
                    230:"110 Hours",
                    231:"111 Hours",
                    232:"112 Hours",
                    233:"113 Hours",
                    234:"114 Hours",
                    235:"115 Hours",
                    236:"116 Hours",
                    237:"117 Hours",
                    238:"118 Hours",
                    239:"119 Hours",
                    240:"120 Hours",
                    241:"121 Hours",
                    242:"122 Hours",
                    243:"123 Hours",
                    244:"124 Hours",
                    245:"125 Hours",
                    246:"126 Hours",
                    247:"127 Hours",
                    248:"128 Hours",
                    249:"129 Hours",
                    250:"130 Hours",
                    251:"131 Hours",
                    252:"132 Hours",
                    253:"133 Hours",
                    254:"134 Hours"]
                input "parameter8-${i}d", "enum", title: "LED Effect Type - Notification $i", description: "Tap to set", displayDuringSetup: false, required: false, options: [
                    0:"Off",
                    1:"Solid",
                    //2:"Chase",
                    2:"Fast Blink",
                    3:"Slow Blink",
                    4:"Pulse"]
    
    }
    input "disableLocal", "enum", title: "Disable Local Control", description: "\nDisable ability to control switch from the wall", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input "disableRemote", "enum", title: "Disable Remote Control", description: "\nDisable ability to control switch from inside Hubitat", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
    input description: "Use the below options to enable child devices for the specified settings. This will allow you to adjust these settings using Apps such as Rule Machine.", title: "Child Devices", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    input "enableLEDChild", "bool", title: "Create \"LED Color\" Child Device", description: "", required: false, defaultValue: false                                                           // segv11: was true
    input "enableDisableLocalChild", "bool", title: "Create \"Disable Local Control\" Child Device", description: "", required: false, defaultValue: false
    input "enableDisableRemoteChild", "bool", title: "Create \"Disable Remote Control\" Child Device", description: "", required: false, defaultValue: false
    input name: "debugEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    input name: "infoEnable", type: "bool", title: "Enable informational logging", defaultValue: true
    input name: "disableDebugLogging", type: "number", title: "Disable Debug Logging", description: "Disable debug logging after this number of minutes (0=Do not disable)", defaultValue: 1440 // segv11: was 0
    input name: "disableInfoLogging", type: "number", title: "Disable Info Logging", description: "Disable info logging after this number of minutes (0=Do not disable)", defaultValue: 1440    // segv11: was 30
    input name: "renameChildren", type: "bool", title: "Rename Child Devices on Reconfiguration", defaultValue: true                                                                            // segv11: NEW
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private sendAlert(data) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Error while creating child device"
    sendEvent(
        descriptionText: data.message,
        eventType: "ALERT",
        name: "failedOperation",
        value: "failed",
        displayed: true,
    )
}

def debugLogsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling debug logging after timeout"
    device.updateSetting("debugEnable",[value:"false",type:"bool"])
}

def infoLogsOff(){
    log.warn "${device.label?device.label:device.name}: Disabling info logging after timeout"
    device.updateSetting("infoEnable",[value:"false",type:"bool"])
}

private toggleTiles(number, value) {
   for (int i = 1; i <= 5; i++){
       if ("${i}" != number){
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: "off")
           }
       } else {
           def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$i"}
           if (childDevice) {         
                childDevice.sendEvent(name: "switch", value: value)
           }
       }
   }
}

def setIndicator(value){
    if (infoEnable) log.info "${device.label?device.label:device.name}: setIndicator($value)"
    return startNotification(value)
}

def startNotification(value, ep = null){
    if (infoEnable) log.info "${device.label?device.label:device.name}: startNotification($value)"
    def parameterNumbers = [8]
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),4), parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1])
    return commands(cmds)
}

def stopNotification(ep = null){
    if (infoEnable) log.info "${device.label?device.label:device.name}: stopNotification()"
    def parameterNumbers = [8]
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(0,4), parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1], size: 4)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterNumbers[(ep == null)? 0:ep?.toInteger()-1])
    return commands(cmds)
}

def componentSetColor(cd,value) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentSetColor($value)"
	if (value.hue == null || value.saturation == null) return
	def ledColor = Math.round(huePercentToZwaveValue(value.hue))
	if (infoEnable) log.info "${device.label?device.label:device.name}: Setting LED color value to $ledColor & LED intensity to $ledLevel"
    def cmds = []
    if (value.level != null) {
        def ledLevel = Math.round(value.level/10)
        cmds << setParameter(6, ledLevel, 1)
        cmds << getParameter(6)
    }
    cmds << setParameter(5, ledColor, 2)
    cmds << getParameter(5)
    return commands(cmds)
}

def componentSetColorTemperature(cd, value, level = null, duration = null) {
    if (infoEnable != "false") log.info "${device.label?device.label:device.name}: cd, componentSetColorTemperature($value)"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Setting LED color value to 255"
    state.colorTemperature = value
    def cmds = []
    if (level != null) {
        def ledLevel = Math.round(level/10)
        cmds << setParameter(6, ledLevel, 1)
        cmds << getParameter(6)
    }
    cmds << setParameter(5, 255, 2)
    cmds << getParameter(5)
    if(cmds) commands(cmds)
}

private huePercentToValue(value){
    return value<=2?0:(value>=98?360:value/100*360)
}

private hueValueToZwaveValue(value){
    return value<=2?0:(value>=356?255:value/360*255)
}

private huePercentToZwaveValue(value){
    return value<=2?0:(value>=98?254:value/100*255)
}

private zwaveValueToHueValue(value){
    return value<=2?0:(value>=254?360:value/255*360)
}

private zwaveValueToHuePercent(value){
    return value<=2?0:(value>=254?100:value/255*100)
}

def childSetLevel(String dni, value) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childSetLevel($dni, $value)"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)    
    def cmds = []
    switch (channelNumber(dni)) {
        case 101:
            cmds << zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 1 : 0, rfProtectionState: state.rfProtectionState? state.rfProtectionState:0)
            cmds << zwave.protectionV2.protectionGet()
        break
        case 102:
            cmds << zwave.protectionV2.protectionSet(localProtectionState : state.localProtectionState? state.localProtectionState:0, rfProtectionState : level > 0 ? 1 : 0)
            cmds << zwave.protectionV2.protectionGet()
        break
        case 103:
            cmds << setParameter(6, Math.round(level/10), 1)
            cmds << getParameter(6)
        break
        case 104:
            cmds << setParameter(7, Math.round(level/10), 1)
            cmds << getParameter(7)
        break
    }
	return commands(cmds)
}

def childOn(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOn($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "on")
        cmds << new hubitat.device.HubAction(command(setParameter(8, calculateParameter("8-${channelNumber(dni)}"), 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 99)
    }
}

def childOff(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childOff($dni)"
    def cmds = []
    if(channelNumber(dni).toInteger() <= 5) {
        toggleTiles("${channelNumber(dni)}", "off")
        cmds << new hubitat.device.HubAction(command(setParameter(8, 0, 4)), hubitat.device.Protocol.ZWAVE )
        return cmds
    } else {
        childSetLevel(dni, 0)
    }
}

def componentOn(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentOn($cd)"
    return childOn(cd.deviceNetworkId)
}

def componentOff(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentOff($cd)"
    return childOff(cd.deviceNetworkId)
}

def componentSetLevel(cd,level,transitionTime = null) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentSetLevel($cd, $value)"
	return childSetLevel(cd.deviceNetworkId,level)
}

void componentRefresh(cd) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: componentRefresh($cd)"
}

void childRefresh(String dni) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: childRefresh($dni)"
}

def childExists(ep) {
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith(ep)}
    if (childDevice) 
        return true
    else
        return false
}

def installed() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: installed()"
    refresh()
}

def configure() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated()"
        if (debugEnable && disableDebugLogging) runIn(disableDebugLogging*60,debugLogsOff)
        if (infoEnable && disableInfoLogging) runIn(disableInfoLogging*60,infoLogsOff)
        state.lastRan = now()
        def cmds = initialize()
        if (cmds != [])
            commands(cmds, 1000)
        else 
            return null
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: updated() ran within the last 2 seconds. Skipping execution."
    }
}

private addChild(id, label, namespace, driver, isComponent){
    if(!childExists(id)){
        try {
            def newChild = addChildDevice(namespace, driver, "${device.deviceNetworkId}-${id}", 
                    [completedSetup: true, label: "${device.displayName} (${label})",
                    isComponent: isComponent, componentName: id, componentLabel: label])
            newChild.sendEvent(name:"switch", value:"off")
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the driver for \"${driver}\" with a namespace of ${namespace} is installed"]])
        }
    }
}

private deleteChild(id){
    if(childExists(id)){
        def childDevice = childDevices.find{it.deviceNetworkId.endsWith(id)}
        try {
            if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            if (infoEnable) log.info "Hubitat may have issues trying to delete the child device when it is in use. Need to manually delete them."
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any App."]])
        }
    }
}

def initialize() {
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    
    if (enableDisableLocalChild) addChild("ep101", "Disable Local Control", "hubitat", "Generic Component Switch", false)
    else deleteChild("ep101")
    if (enableDisableRemoteChild) addChild("ep102", "Disable Remote Control", "hubitat", "Generic Component Switch", false)
    else deleteChild("ep102")
    if (enableLEDChild) addChild("ep103", "LED Color", "hubitat", "Generic Component RGBW", false)
    else deleteChild("ep103")
    if (enableLED1OffChild) addChild("ep104", "LED - When Off", "hubitat", "Generic Component Dimmer", false)
    else deleteChild("ep104")
    
    [1,2,3,4,5].each { i ->
        if ((settings."parameter8-${i}a"!=null && settings."parameter8-${i}b"!=null && settings."parameter8-${i}c"!=null && settings."parameter8-${i}d"!=null && settings."parameter8-${i}d"!="0") && !childExists("ep${i}")) {
            addChild("ep${i}", "Notification ${i}", "hubitat", "Generic Component Switch", false)
        } else if ((settings."parameter8-${i}a"==null || settings."parameter8-${i}b"==null || settings."parameter8-${i}c"==null || settings."parameter8-${i}d"==null || settings."parameter8-${i}d"=="0") && childExists("ep${i}")) {
            deleteChild("ep${i}")
        }
    }
    
    if (device.label != state.oldLabel && renameChildren) {                               // segv11: NEW -- I don't like the renaming
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep1")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 1)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep2")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 2)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep3")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 3)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep4")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 4)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep5")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Notification 5)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Remote Control)")
    }
    state.oldLabel = device.label
    
    def cmds = processAssociations()
    
    getParameterNumbers().each{ i ->
      if ((state."parameter${i}value" != ((settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger()))){
          cmds << setParameter(i, (settings."parameter${i}"!=null||calculateParameter(i)!=null)? calculateParameter(i).toInteger() : getParameterInfo(i, "default").toInteger(), getParameterInfo(i, "size").toInteger())
          cmds << getParameter(i)
      }
      else {
          //if (infoEnable) log.info "${device.label?device.label:device.name}: Parameter already set"
      }
    }
    
    if (state."parameter9value" != 0){
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 9, size: 1)
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 9)
    }
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.localProtectionState?.toInteger() != settings.disableLocal?.toInteger() || state.rfProtectionState?.toInteger() != settings.disableRemote?.toInteger()) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Protection command class settings need to be updated"
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: disableRemote!=null? disableRemote.toInteger() : 0)
        cmds << zwave.protectionV2.protectionGet()
    } else {
        if (infoEnable) log.info "${device.label?device.label:device.name}: No Protection command class settings to update"
    }

    if (cmds != []) return cmds else return []
}

def calculateParameter(number) {
    def value = 0
    switch (number){
      case "5":
          if (settings.parameter5) value = settings."parameter${number}"
          else if (settings.parameter5custom =~ /^([0-9]{1}|[0-9]{2}|[0-9]{3})$/) value = hueValueToZwaveValue(settings.parameter5custom.toInteger())
          else value = settings."parameter${number}"
      break
      case "8-1":
      case "8-2":
      case "8-3": 
      case "8-4":
      case "8-5":
         value += settings."parameter${number}a"!=null ? settings."parameter${number}a".toInteger() * 1 : 0
         value += settings."parameter${number}b"!=null ? settings."parameter${number}b".toInteger() * 256 : 0
         value += settings."parameter${number}c"!=null ? settings."parameter${number}c".toInteger() * 65536 : 0
         value += settings."parameter${number}d"!=null ? settings."parameter${number}d".toInteger() * 16777216 : 0
      break
      default:
          value = settings."parameter${number}"
      break
    }
    return value
}

def setConfigParameter(number, value, size) {
    return command(setParameter(number, value, size.toInteger()))
}

def setParameter(number, value, size) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Setting parameter $number with a size of $size bytes to $value"
    return zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(value.toInteger(),size), parameterNumber: number, size: size)
}

def getParameter(number) {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Retreiving value of parameter $number"
    return zwave.configurationV1.configurationGet(parameterNumber: number)
}

def getParameterNumbers(){
    return [1,2,3,4,5,6,7,10,11,12,13,51]
}

def getParameterInfo(number, type){
    def parameter = [:]

    parameter.parameter1default=0
    parameter.parameter2default=0
    parameter.parameter3default=0
    parameter.parameter4default=15
    parameter.parameter5default=170
    parameter.parameter6default=5
    parameter.parameter7default=1
    parameter.parameter8default=0
    parameter.parameter9default=0
    parameter.parameter10default=10
    parameter.parameter11default=3600
    parameter.parameter12default=10
    parameter.parameter13default=0
    parameter.parameter51default=1
    
    parameter.parameter1type="enum"
    parameter.parameter2type="enum"
    parameter.parameter3type="number"
    parameter.parameter4type="number"
    parameter.parameter5type="enum"
    parameter.parameter6type="enum"
    parameter.parameter7type="enum"
    parameter.parameter8type="enum"
    parameter.parameter9type="enum"
    parameter.parameter10type="number"
    parameter.parameter11type="number"
    parameter.parameter12type="number"
    parameter.parameter13type="enum"
    parameter.parameter51type="enum"
    
    parameter.parameter1size=1
    parameter.parameter2size=1
    parameter.parameter3size=2
    parameter.parameter4size=1
    parameter.parameter5size=2
    parameter.parameter6size=1
    parameter.parameter7size=1
    parameter.parameter8size=4
    parameter.parameter9size=1
    parameter.parameter10size=1
    parameter.parameter11size=2
    parameter.parameter12size=1
    parameter.parameter13size=1
    parameter.parameter51size=1
    
    parameter.parameter1options=["0":"Previous", "1":"On", "2":"Off"]
    parameter.parameter2options=["1":"Yes", "0":"No"]
    parameter.parameter3options="1..32767"
    parameter.parameter4options="0..15"
    parameter.parameter5options=["0":"Red","21":"Orange","42":"Yellow","85":"Green","127":"Cyan","170":"Blue","212":"Violet","234":"Pink", "255":"White (Firmware 1.19+)"]
    parameter.parameter6options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter7options=["0":"0%","1":"10%","2":"20%","3":"30%","4":"40%","5":"50%","6":"60%","7":"70%","8":"80%","9":"90%","10":"100%"]
    parameter.parameter8options=["1":"Yes", "2":"No"]
    parameter.parameter9options=["0":"Stay Off","1":"1 Second","2":"2 Seconds","3":"3 Seconds","4":"4 Seconds","5":"5 Seconds","6":"6 Seconds","7":"7 Seconds","8":"8 Seconds","9":"9 Seconds","10":"10 Seconds"]
    parameter.parameter10options="0..100"
    parameter.parameter11options="0..32767"
    parameter.parameter12options="0..100"
    parameter.parameter13options=["0":"Default", "1":"Special Load (T8)"]
    parameter.parameter51options=["0":"Yes", "1":"No (Default)"]
    
    parameter.parameter1name="State After Power Restored"
    parameter.parameter2name="Invert Switch"
    parameter.parameter3name="Auto Off Timer"
    parameter.parameter4name="Association Behavior"
    parameter.parameter5name="LED Strip Color"
    parameter.parameter6name="LED Strip Intensity"
    parameter.parameter7name="LED Strip Intensity (When OFF)"
    parameter.parameter8name="LED Strip Effect"
    parameter.parameter9name="LED Strip Timeout"
    parameter.parameter10name="Active Power Reports"
    parameter.parameter11name="Periodic Power & Energy Reports"
    parameter.parameter12name="Energy Reports"
    parameter.parameter13name="Load Type"
    parameter.parameter51name="Disable Physical On/Off Delay"
    
    
    parameter.parameter1description="The state the switch should return to once power is restored after power failure."
    parameter.parameter2description="Inverts the orientation of the switch. Useful when the switch is installed upside down. Essentially up becomes down and down becomes up."
    parameter.parameter3description="Automatically turns the switch off after this many seconds. When the switch is turned on a timer is started that is the duration of this setting. When the timer expires, the switch is turned off."
    parameter.parameter4description="When should the switch send commands to associated devices?\n\n01 - local\n02 - 3way\n03 - 3way & local\n04 - z-wave hub\n05 - z-wave hub & local\n06 - z-wave hub & 3-way\n07 - z-wave hub & local & 3way\n08 - timer\n09 - timer & local\n10 - timer & 3-way\n11 - timer & 3-way & local\n12 - timer & z-wave hub\n13 - timer & z-wave hub & local\n14 - timer & z-wave hub & 3-way\n15 - all"
    parameter.parameter5description="This is the color of the LED strip."
    parameter.parameter6description="This is the intensity of the LED strip."
    parameter.parameter7description="This is the intensity of the LED strip when the switch is off. This is useful for users to see the light switch location when the lights are off."
    parameter.parameter8description="LED Strip Effect"
    parameter.parameter9description="When the LED strip is disabled (LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted."
    parameter.parameter10description="The power level change that will result in a new power report being sent. The value is a percentage of the previous report. 0 = disabled."
    parameter.parameter11description="Time period between consecutive power & energy reports being sent (in seconds). The timer is reset after each report is sent."
    parameter.parameter12description="The energy level change that will result in a new energy report being sent. The value is a percentage of the previous report."
    parameter.parameter13description="The default of the switch is to auto detect the load. In some situations you may want to try the option for a special load type. (firmware 1.17+)"
    parameter.parameter51description="The 700ms delay that occurs after pressing the physical button to turn the switch on/off is removed. Consequently this also removes the following scenes: held, released, 2x, 3x, 4x, 5x tap. 1x tap and config button scenes still work. (firmware 1.19+)"
    
    return parameter."parameter${number}${type}"
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def event
	if (cmd.scale == 0) {
    	if (cmd.meterType == 161) {
		    event = createEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Voltage report received with value of ${cmd.scaledMeterValue} V"
        } else if (cmd.meterType == 1) {
        	event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
            if (infoEnable) log.info "${device.label?device.label:device.name}: Energy report received with value of ${cmd.scaledMeterValue} kWh"
            /*** segv11 meterExtra */
            if (device.currentValue("energyTime")) {
                long energyTime = device.currentValue("energyTime").toLong()
                def energyDuration = TimeCategory.minus(new Date(), new Date(energyTime)).toString()
                sendEvent(name: "energyDuration", value: (energyDuration.toString()), displayed:false)
            }
            /* segv11 meterExtra ***/
        }
	} else if (cmd.scale == 1) {
		event = createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Amperage report received with value of ${cmd.scaledMeterValue} A"
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        if (infoEnable) log.info "${device.label?device.label:device.name}: Power report received with value of ${cmd.scaledMeterValue} W"
        /*** segv11 meterExtra */
        Double powerHigh, powerLow;
        try {
            powerHigh = device.currentValue("powerHigh").toDouble()
            if (infoEnable) log.info "old powerHigh: $powerHigh W"
            powerHigh = Math.max(powerHigh, cmd.scaledMeterValue)
            if (infoEnable) log.info "new powerHigh: $powerHigh W"
             if (powerHigh < 0) powerHigh = 0/0
       } catch(Exception ex) {
            powerHigh = cmd.scaledMeterValue
            if (infoEnable) log.info "no old powerHigh; new powerHigh: $powerHigh W"
       }
       try {
            powerLow = device.currentValue("powerLow").toDouble()
            if (infoEnable) log.info "old powerLow: $powerLow W"
            powerLow = Math.min(powerLow, cmd.scaledMeterValue)
            if (infoEnable) log.info "new powerLow: $powerLow W"
            if (powerLow < 0) powerLow = 0/0
       } catch(Exception ex) {
            powerLow = cmd.scaledMeterValue
            if (infoEnable) log.info "no old powerLow; new powerLow: $powerLow W"
       }
       sendEvent(name:"powerHigh", value: Math.round(powerHigh), unit: "W"); /* meterExtra2 */
       sendEvent(name:"powerLow", value: Math.round(powerLow),  unit: "W");  /* meterExtra2 */
       /* segv11 meterExtra ***/
	}

    return event
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
    def integerValue = cmd2Integer(cmd.configurationValue)
    state."parameter${cmd.parameterNumber}value" = cmd2Integer(cmd.configurationValue)
    switch (cmd.parameterNumber) {
        case 5:
            if(integerValue==0||integerValue==21||integerValue==42||integerValue==85||integerValue==127||integerValue==170||integerValue==212||integerValue==234||integerValue==255){
                device.updateSetting("parameter${cmd.parameterNumber}",[value:"${integerValue}",type:"number"])
                device.removeSetting("parameter${cmd.parameterNumber}custom")
            } else {
                device.removeSetting("parameter${cmd.parameterNumber}")
                device.updateSetting("parameter${cmd.parameterNumber}custom",[value:Math.round(zwaveValueToHueValue(integerValue)),type:"number"])
            }
            def childDevice = childDevices.find{it.deviceNetworkId.endsWith("ep103")}
            if (childDevice) {
                childDevice.sendEvent(name:"hue", value:"${Math.round(zwaveValueToHuePercent(integerValue))}")
                childDevice.sendEvent(name:"saturation", value:"100")
            }
        break
        case 6:
            device.updateSetting("parameter${cmd.parameterNumber}",[value:"${integerValue}",type:"enum"])
            def childDevice = childDevices.find{it.deviceNetworkId.endsWith("ep103")}
            if (childDevice) {
                childDevice.sendEvent(name:"level", value:"${integerValue*10}")
                childDevice.sendEvent(name:"switch", value:"${integerValue==0?"off":"on"}")
            }
        break
    }
}

def cmd2Integer(array) {
    switch(array.size()) {
        case 1:
            array[0]
            break
        case 2:
            ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
            break
        case 3:
            ((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
            break
        case 4:
            ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
            break
    }
}

def integer2Cmd(value, size) {
    try{
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
    } catch (e) {
        if (infoEnable) log.info "${device.label?device.label:device.name}: Error: integer2Cmd $e Value: $value"
    }
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1, 0x32: 3, 0x5B: 1]
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def parse(description) {
    def result = null
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    } else if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
            //if (debugEnable) log.debug("'$cmd' parsed to $result")
        } else {
            if (debugEnable) log.debug("Couldn't zwave.parse '$description'")
        }
    }
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic report received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Basic set received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Switch Binary report received with value of ${cmd.value ? "on" : "off"}"
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

void zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    switch (cmd.keyAttributes) {
       case 0:
       if (cmd.sceneNumber == 3) buttonEvent(7, "pushed", "physical")
       else buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical")
       break
       case 1:
       buttonEvent(6, (cmd.sceneNumber == 2? "pushed" : "held"), "physical")
       break
       case 2:
       buttonEvent(8, (cmd.sceneNumber == 2? "pushed" : "held"), "physical")
       break
       default:
       buttonEvent(cmd.keyAttributes - 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical")
       break
    }
}

void buttonEvent(button, value, type = "digital") {
    switch (button) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Tap '.padRight(button+5, '▼'):' Tap '.padRight(button+5, '▲')}", displayed:false)
        break
        case 6:
            sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Released ▼':' Released ▲'}", displayed:false)
        break
        case 8:
            sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Hold ▼':' Hold ▲'}", displayed:false)
        break
    }
        
    if (infoEnable) log.info "${device.label?device.label:device.name}: Button ${button} was ${value}"
    
    sendEvent(name: value, value: button, isStateChange:true)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: Unhandled: $cmd"
    null
}

def reset() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: Resetting energy statistics"
    /*** segv11 meterExtra */
    sendEvent(name:"energyTime",  value: new Date().getTime(), displayed:false)
    sendEvent(name:"powerHigh", value: -1, displayed: false);
    sendEvent(name:"powerLow", value: -1, displayed: false);
    /* segv11 meterExtra ***/
	def cmds = []
    cmds << zwave.meterV2.meterReset()
    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	commands(cmds, 1000)
}

def on() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: on()"
    commands([
        zwave.basicV1.basicSet(value: 0xFF)//,
        //zwave.basicV1.basicGet()
    ])
}

def off() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: off()"
    commands([
        zwave.basicV1.basicSet(value: 0x00)//,
        //zwave.basicV1.basicGet()
    ])
}

def ping() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: ping()"
    //refresh()
}

def poll() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: poll()"
    //refresh()
}

def refresh() {
    if (infoEnable) log.info "${device.label?device.label:device.name}: refresh()"
    def cmds = []
    cmds << zwave.basicV1.basicGet()
    cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
    cmds << zwave.protectionV2.protectionGet()
    return commands(cmds)
}

private command(hubitat.zwave.Command cmd) {
    return zwaveSecureEncap(cmd)
}

void zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd){
    hubitat.zwave.Command encapCmd = cmd.encapsulatedCommand(commandClassVersions)
    if (encapCmd) {
        zwaveEvent(encapCmd)
    }
    sendHubCommand(new hubitat.device.HubAction(command(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
}

private commands(commands, delay=500) {
    delayBetween(commands.collect{ command(it) }, delay)
}

def pressUpX1() {
    buttonEvent(1, "pushed")
}

def pressDownX1() {
    buttonEvent(1, "held")
}

def pressUpX2() {
    buttonEvent(2, "pushed")
}

def pressDownX2() {
    buttonEvent(2, "held")
}

def pressUpX3() {
    buttonEvent(3, "pushed")
}

def pressDownX3() {
    buttonEvent(3, "held")
}

def pressUpX4() {
    buttonEvent(4, "pushed")
}

def pressDownX4() {
    buttonEvent(4, "held")
}

def pressUpX5() {
    buttonEvent(5, "pushed")
}

def pressDownX5() {
    buttonEvent(5, "held")
}

def holdUp() {
    buttonEvent(8, "pushed")
}

def holdDown() {
    buttonEvent(8, "held")
}

def releaseUp() {
    buttonEvent(6, "pushed")
}

def releaseDown() {
    buttonEvent(6, "held")
}

def pressConfig() {
    buttonEvent(7, "pushed")
}

void push(button) {
   sendEvent(name: "pushed", value: button, isStateChange: true, type: "digital")
}

void hold(button) {
   sendEvent(name: "held", value: button, isStateChange: true, type: "digital")
}

def setDefaultAssociations() {
    def smartThingsHubID = String.format('%02x', zwaveHubNodeId).toUpperCase()
    state.defaultG1 = [smartThingsHubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    // Normalize the arguments to be backwards compatible with the old method
    action = "${action}" == "1" ? "Add" : "${action}" == "0" ? "Remove" : "${action}" // convert 1/0 to Add/Remove
    group  = "${group}" =~ /\d+/ ? (group as int) : group                             // convert group to int (if possible)
    nodes  = [] + nodes ?: [nodes]                                                    // convert to collection if not already a collection

    if (! nodes.every { it =~ /[0-9A-F]+/ }) {
        log.error "${device.label?device.label:device.name}: invalid Nodes ${nodes}"
        return
    }

    if (group < 1 || group > maxAssociationGroup()) {
        log.error "${device.label?device.label:device.name}: Association group is invalid 1 <= ${group} <= ${maxAssociationGroup()}"
        return
    }
    
    def associations = state."desiredAssociation${group}"?:[]
    nodes.each { 
        node = "${it}"
        switch (action) {
            case "Remove":
            if (infoEnable) log.info "${device.label?device.label:device.name}: Removing node ${node} from association group ${group}"
            associations = associations - node
            break
            case "Add":
            if (infoEnable) log.info "${device.label?device.label:device.name}: Adding node ${node} to association group ${group}"
            associations << node
            break
        }
    }
    state."desiredAssociation${group}" = associations.unique()
    return
}

def maxAssociationGroup(){
   if (!state.associationGroups) {
       if (infoEnable) log.info "${device.label?device.label:device.name}: Getting supported association groups from device"
       sendHubCommand(new hubitat.device.HubAction(command(zwave.associationV2.associationGroupingsGet()), hubitat.device.Protocol.ZWAVE )) // execute the update immediately
   }
   (state.associationGroups?: 5) as int
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def associationGroups = maxAssociationGroup()
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (it){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Adding node $it to group $i"
                    cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                    refreshGroup = true
                }
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (it){
                    if (infoEnable) log.info "${device.label?device.label:device.name}: Removing node $it from group $i"
                    cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:hubitat.helper.HexUtils.hexStringToInt(it))
                    refreshGroup = true
                }
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else if (infoEnable) log.info "${device.label?device.label:device.name}: There are no association actions to complete for group $i"
         }
      } else {
         if (infoEnable) log.info "${device.label?device.label:device.name}: Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (infoEnable) log.info "${device.label?device.label:device.name}: Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (infoEnable) log.info "${device.label?device.label:device.name}: Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if(cmd.applicationVersion != null && cmd.applicationSubVersion != null) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        if (infoEnable) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    } else if(cmd.firmware0Version != null && cmd.firmware0SubVersion != null) {
	    def firmware = "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${device.label?device.label:device.name}: ${cmd}"
    if (infoEnable) log.info "${device.label?device.label:device.name}: Protection report received: Local protection is ${cmd.localProtectionState > 0 ? "on" : "off"} & Remote protection is ${cmd.rfProtectionState > 0 ? "on" : "off"}"
    state.localProtectionState = cmd.localProtectionState
    state.rfProtectionState = cmd.rfProtectionState
    device.updateSetting("disableLocal",[value:cmd.localProtectionState?"1":"0",type:"enum"])
    device.updateSetting("disableRemote",[value:cmd.rfProtectionState?"1":"0",type:"enum"])
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.localProtectionState > 0 ? "on" : "off")        
    }
    childDevice = children.find{it.deviceNetworkId.endsWith("ep102")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: cmd.rfProtectionState > 0 ? "on" : "off")        
    }
}
