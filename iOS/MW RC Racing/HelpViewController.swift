//
//  HelpViewController.swift
//  MW RC Racing
//
//  Created by Marco Bolduc on 2016-11-15.
//  Copyright © 2016 Meade Willis. All rights reserved.
//

import UIKit

class HelpViewController: UIViewController {
    
    @IBOutlet weak var backButton: CustomButton!
    
    @IBAction func backPressed(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
}
