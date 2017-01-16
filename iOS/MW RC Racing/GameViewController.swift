//
//  GameViewController.swift
//  MW RC Racing
//
//  Created by Marco Bolduc on 2016-11-16.
//  Copyright © 2016 Meade Willis. All rights reserved.
//

import UIKit
import AVFoundation

class GameViewController: UIViewController {
    
    let synth = AVSpeechSynthesizer()
    var myUtterance = AVSpeechUtterance(string: "")
    
    var isConnected = false
    var allowTX = true
    var initialized = false
    
    var gatesNumber : Int = -1
    var lapsNumber : Int = -1
    var killsNumber : Int = -1
    var raceType : Int = -1
    
    class Player {
        var id : Int
        var active : Bool
        var name : String
        var lastGate : Int
        var totalGates : Int
        var totalLaps : Int
        var deaths : Int
        var kills : Int
        var position : Int
        var finish : Int
        var labelText : String
        init() {
            self.id = -1
            self.active = false
            self.name = ""
            self.lastGate = 0
            self.totalGates = -1
            self.totalLaps = 0
            self.deaths = 0
            self.kills = 0
            self.position = 0
            self.finish = 0
            self.labelText = ""
        }
    }
    var winnersColor: [UIColor] = [#colorLiteral(red: 0.9529411793, green: 0.6862745285, blue: 0.1333333403, alpha: 1),#colorLiteral(red: 0.6000000238, green: 0.6000000238, blue: 0.6000000238, alpha: 1),#colorLiteral(red: 0.7450980544, green: 0.1568627506, blue: 0.07450980693, alpha: 1)]
    var winnersString: [String] = ["WINNER", "2nd Position", "3rd Position", "Position #4", "Position #5"]
    
    var Players = [Player]()
    var sortedPlayers = [Player]()
    
    var oldName = ""
    
    var currentPlayers = 0
    var playersFinish = 0
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var backButton: CustomButton!
    @IBOutlet weak var nameButton: CustomButton!
    @IBOutlet weak var btStatus: UIImageView!
    
    @IBOutlet weak var t1: CustomLabel!
    @IBOutlet weak var t2: CustomLabel!
    @IBOutlet weak var t3: CustomLabel!
    @IBOutlet weak var t4: CustomLabel!
    @IBOutlet weak var t5: CustomLabel!
    
    @IBAction func backPressed(_ sender: Any) {
        var title = "Stop this race ?"
        if currentPlayers == playersFinish {
            title = "Start new race ?"
        }
        tts(title,0.45)
        let alert = UIAlertController(title: title, message: nil, preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Yes", style: UIAlertActionStyle.destructive, handler: { action in
            self.dismiss(animated: true, completion: nil)
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    @IBAction func namePressed(_ sender: Any) {
        if currentPlayers > 0 {
            let alert = UIAlertController(title: "Select RC truck number", message: "", preferredStyle: .alert)
            for i in 0...(currentPlayers-1) {
                alert.addAction(UIAlertAction(title: String(self.sortedPlayers[i].id+1), style: .default, handler: { (UIAlertAction) in self.changeTruckName(self.sortedPlayers[i].id) }))
            }
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler:nil))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    func changeTruckName(_ id : Int) {
        let s : String = String(id+1) + "C4"
        sendMessageToDevice(s)
        func configurationTextField(textField: UITextField!) { tField = textField }
        func changeName() {
            self.oldName = self.Players[id].name
            self.Players[id].name = self.tField.text!
            self.updateUIPanel(id)
            self.tts(self.oldName + " prefer to be call " + Players[id].name + " now.",0.47)
        }
        let alert = UIAlertController(title: "Truck #" + String(id+1) + " driver name", message: "max 10 char", preferredStyle: .alert)
        alert.addTextField(configurationHandler: configurationTextField)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler:nil))
        alert.addAction(UIAlertAction(title: "Done", style: .destructive, handler:{ (UIAlertAction) in changeName() }))
        self.present(alert, animated: true, completion: { })
    }
    
    var tField: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        if raceType == 1 {
            titleLabel.text = "Race without guns"
            subtitleLabel.text = String(lapsNumber) + " laps with " + String(gatesNumber) + " gates"
        } else if raceType == 2 {
            titleLabel.text = "Race with guns"
            subtitleLabel.text = String(lapsNumber) + " laps with " + String(gatesNumber) + " gates"
        } else if raceType == 3 {
            titleLabel.text = "Search & destroy"
            subtitleLabel.text = "First with " + String(killsNumber) + " kills"
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(GameViewController.messageReceived(_:)), name: NSNotification.Name(rawValue: BLEServiceMessageReceived), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(GameViewController.connectionChanged(_:)), name: NSNotification.Name(rawValue: BLEServiceChangedStatusNotification), object: nil)
        
        _ = btDiscoverySharedInstance
        
        if btDiscoverySharedInstance.bleService?.isConnected == true {
            self.btStatus.image = UIImage(named: "Bluetooth_Connected")
            for i in 0...4 {
                Players.append(Player())
                sendMessageToDevice(String(i+1) + "C0")
            }
        }
        var text = titleLabel.text! + ", " + subtitleLabel.text!
        if raceType < 3 {
            text += " per lap"
        } else {
            text += " win the battle"
        }
        tts(text,0.48)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: BLEServiceMessageReceived), object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: BLEServiceChangedStatusNotification), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if btDiscoverySharedInstance.isSupported == false {
            let alert = UIAlertController(title: "This device does not support Bluetooth Low Energy", message: "", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Exit", style: .destructive, handler:{ (UIAlertAction) in exit(0) }))
            self.present(alert, animated: true, completion: { })
        }
    }
    
    func tts(_ text: String, _ speed: Float) {
        myUtterance = AVSpeechUtterance(string: text)
        myUtterance.rate = speed
        myUtterance.voice = AVSpeechSynthesisVoice(language: "en-US")
        synth.speak(myUtterance)
    }
    
    func tts(_ text: String) {
        myUtterance = AVSpeechUtterance(string: text)
        myUtterance.rate = 0.52
        myUtterance.voice = AVSpeechSynthesisVoice(language: "en-US")
        synth.speak(myUtterance)
    }
    
    func handleMessage(_ c: [Character]) {
        let id = Int(String(c[0]))! - 1
        if id > 4 || id < 0 { return }
        let p: Player = Players[id]
        let arg: Int = Int(String(c[2]))!
        if p.active == false && p.finish == 0 {
            p.id = id
            p.active = true
            currentPlayers += 1
            p.name = "Truck " + String(c[0])
            sortedPlayers.append(p)
        }
        else if p.finish > 0 { return }
        
        switch c[1] { // MESSAGE FORM = XYZ (X:FromID Y:Case Z:Argument)
        case "P": // Z notify X is in front of him
            break
            
        case "Z": // X ask for current race type
            let s : String = String(c[0]) + "C" + String(raceType)
            sendMessageToDevice(s)
            var text = " join the race"
            if raceType == 3 {
                text = " join the battle"
            }
            tts(p.name + text,0.49)
            break
            
        case "T": // X change turbo status
            break
            
        case "S": // X change gun status
            break
            
        case "D": // X shot by Z
            if raceType != 1 {
                p.deaths += 1
                var killer = [Character]()
                killer.append(c[2])
                killer.append("K")
                killer.append("1")
                handleMessage(killer)
                p.labelText = " K:" + String(p.kills) + "/" + String(self.killsNumber) + " D:" + String(p.deaths)
                tts(p.name + " has been shot by " + Players[arg-1].name)
            }
            break
            
        case "K": // needed? X shot someone
            if raceType != 1 {
                p.kills += 1
                p.labelText = " K:" + String(p.kills) + "/" + String(self.killsNumber) + " D:" + String(p.deaths)
            }
            if raceType == 3 {
                if p.kills == killsNumber {
                    p.labelText = " - " + winnersString[playersFinish]
                    playersFinish += 1
                    p.finish = playersFinish
                    tts(p.name + " finish in position #" + String(p.finish))
                }
            }
            break
            
        case "G": // X pass gate Z
            if raceType == 1 || raceType == 2 {
                if (arg == (p.lastGate+1) && p.lastGate != gatesNumber) {
                    p.lastGate = arg
                    p.totalGates+=1
                    tts(p.name + " pass gate " + String(arg))
                } else if arg == 1 && p.lastGate == gatesNumber {
                    p.lastGate = 1
                    p.totalLaps+=1
                    if p.totalLaps == lapsNumber {
                        p.labelText = " - " + winnersString[playersFinish]
                        playersFinish += 1
                        p.finish = playersFinish
                        tts(p.name + " finish in position #" + String(p.finish))
                        break
                    }
                    tts(p.name + " complete lap " + String(p.totalLaps))
                }
                p.labelText = " G:" + String(p.lastGate) + "/" + String(self.gatesNumber) + " L:" + String(p.totalLaps) + "/" + String(self.lapsNumber)
            }
            break
            
        default:            
            break
        }
        updateUIPanel(id)
    }
    
    func updateUIPanel(_ id : Int) {
        if currentPlayers > 1 {
            if raceType > 0 && raceType < 3 {
                sortedPlayers.sort{ self.Players[id].totalGates > $1.totalGates }
            } else if raceType == 3 {
                sortedPlayers.sort{ self.Players[id].kills > $1.kills }
            }
        }
        DispatchQueue.main.async(execute: {
            for i in 0...(self.currentPlayers-1) {
                let p : Player = self.sortedPlayers[i]
                self.getView(i).text = p.name + p.labelText
                if p.finish > 0 && p.finish < 4 {
                    self.getView(i).backgroundColor = self.winnersColor[i]
                }
            }
        });
    }

    func getView(_ view : Int) -> CustomLabel {
        var ui : CustomLabel!
        if view == 0 {ui = t1}
        else if view == 1 {ui = t2}
        else if view == 2 {ui = t3}
        else if view == 3 {ui = t4}
        else if view == 4 {ui = t5}
        return ui
    }
    
    func messageReceived(_ notification: Notification) {
        let v : String = (notification as NSNotification).object! as! String
        let c = Array(v.characters)
        var s = [Character]()
        var cp = 0
        for i in c {
            if i != "\r" && i != "\n" && i != "\r\n" {
                s.append(i)
                cp += 1
                if cp == 3 {
                    handleMessage(s)
                    cp = 0
                }
            }
        }
    }
    
    func connectionChanged(_ notification: Notification) {
        let userInfo = (notification as NSNotification).userInfo as! [String: Bool]
        DispatchQueue.main.async(execute: {
            self.isConnected = userInfo["isConnected"]!
            if self.isConnected {
                self.btStatus.image = UIImage(named: "Bluetooth_Connected")
                if self.initialized == false {
                    for i in 0...4 {
                        self.Players.append(Player())
                        self.sendMessageToDevice(String(i+1) + "C0")
                    }
                    self.initialized = true
                }
            } else {
                self.btStatus.image = UIImage(named: "Bluetooth_Disconnected")
            }
        });
    }
    
    func sendMessageToDevice(_ message: String) {
        if !allowTX { return }
        if message == "" { return }
        if let bleService = btDiscoverySharedInstance.bleService {
            bleService.writeMessage(message)
        }
    }    
}
