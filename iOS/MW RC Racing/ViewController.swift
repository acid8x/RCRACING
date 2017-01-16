//
//  ViewController.swift
//  MW RC Racing
//
//  Created by Marco Bolduc on 2016-11-15.
//  Copyright © 2016 Meade Willis. All rights reserved.
//

import UIKit
import AVFoundation

class ViewController: UIViewController {
    
    let synth = AVSpeechSynthesizer()
    var myUtterance = AVSpeechUtterance(string: "")

    @IBOutlet weak var startButton: CustomButton!
    @IBOutlet weak var helpButton: CustomButton!
    @IBOutlet weak var exitButton: CustomButton!
    
    @IBAction func exitPressed(_ sender: Any) {
        exit(0)
    }
    
    @IBAction func helpPressed(_ sender: Any) {
        var viewController:HelpViewController = HelpViewController()
        viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "helpController") as! HelpViewController
        self.present(viewController, animated: true, completion: nil)
    }
    
    @IBAction func startPressed(_ sender: Any) {
        tts("New race.",0.42)
        var viewController:StartViewController = StartViewController()
        viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "startController") as! StartViewController
        self.present(viewController, animated: true, completion: nil)
    }
    
    func tts(_ text: String, _ speed: Float) {
        myUtterance = AVSpeechUtterance(string: text)
        myUtterance.rate = speed
        myUtterance.voice = AVSpeechSynthesisVoice(language: "en-US")
        synth.speak(myUtterance)
    }
    
    override func viewDidLoad() {
        tts("Welcome to M W RC Racing.",0.46)
    }
}

