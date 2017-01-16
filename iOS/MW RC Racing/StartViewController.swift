//
//  StartViewController.swift
//  MW RC Racing
//
//  Created by Marco Bolduc on 2016-11-15.
//  Copyright © 2016 Meade Willis. All rights reserved.
//

import UIKit

class StartViewController: UIViewController {
    
    @IBOutlet weak var backButton: CustomButton!
    @IBOutlet weak var nextButton: CustomButton!
    
    @IBOutlet weak var switch1: UISwitch!
    @IBOutlet weak var switch2: UISwitch!
    @IBOutlet weak var switch3: UISwitch!
    
    @IBOutlet weak var gatesLabel: UILabel!
    @IBOutlet weak var killsLabel: UILabel!
    @IBOutlet weak var lapsLabel: UILabel!
    @IBOutlet weak var gatesSwitch: UIStepper!
    @IBOutlet weak var killsSwitch: UIStepper!
    @IBOutlet weak var lapsSwitch: UIStepper!
    
    @IBAction func s1change(_ sender: Any) {
        if switch1.isOn {
            switch1.setOn(true, animated: true)
            switch2.setOn(false, animated: true)
            switch3.setOn(false, animated: true)
            gatesLabel.isHidden = false
            gatesSwitch.isHidden = false
            killsLabel.isHidden = true
            killsSwitch.isHidden = true
            lapsLabel.isHidden = false
            lapsSwitch.isHidden = false
        } else if switch2.isOn == false && switch3.isOn == false {
            switch1.setOn(true, animated: true)
        }
    }
    
    @IBAction func s2change(_ sender: Any) {
        if switch2.isOn {
            switch1.setOn(false, animated: true)
            switch2.setOn(true, animated: true)
            switch3.setOn(false, animated: true)
            gatesLabel.isHidden = false
            gatesSwitch.isHidden = false
            killsLabel.isHidden = true
            killsSwitch.isHidden = true
            lapsLabel.isHidden = false
            lapsSwitch.isHidden = false
        } else if switch1.isOn == false && switch3.isOn == false {
            switch2.setOn(true, animated: true)
        }
    }
    
    @IBAction func s3change(_ sender: Any) {
        if switch3.isOn {
            switch1.setOn(false, animated: true)
            switch2.setOn(false, animated: true)
            switch3.setOn(true, animated: true)
            gatesLabel.isHidden = true
            gatesSwitch.isHidden = true
            killsLabel.isHidden = false
            killsSwitch.isHidden = false
            lapsLabel.isHidden = true
            lapsSwitch.isHidden = true
        } else if switch2.isOn == false && switch1.isOn == false {
            switch3.setOn(true, animated: true)
        }
    }
    
    @IBAction func gatesChange(_ sender: Any) {
        gatesLabel.text = String(Int(gatesSwitch.value)) + " Gates"
    }
    
    @IBAction func killsChange(_ sender: Any) {
        killsLabel.text = String(Int(killsSwitch.value)) + " Kills"
    }
    
    @IBAction func lapsChange(_ sender: Any) {
        lapsLabel.text = String(Int(lapsSwitch.value)) + " Laps"
    }
    
    @IBAction func backPressed(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func nextPressed(_ sender: Any) {
        var viewController:GameViewController = GameViewController()
        viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "gameController") as! GameViewController
        viewController.gatesNumber = Int(gatesSwitch.value)
        viewController.lapsNumber = Int(lapsSwitch.value)
        viewController.killsNumber = Int(killsSwitch.value)
        if switch1.isOn {
            viewController.raceType = 1
        } else if switch2.isOn {
            viewController.raceType = 2
        } else if switch3.isOn {
            viewController.raceType = 3
        }
        self.present(viewController, animated: true, completion: nil)
    }
}
