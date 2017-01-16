//
//  CustomButton.swift
//  MW RC Racing
//
//  Created by Marco Bolduc on 2016-11-15.
//  Copyright © 2016 Meade Willis. All rights reserved.
//

import UIKit

class CustomButton: UIButton {
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        self.layer.cornerRadius = 10
        self.layer.shadowColor = UIColor.black.cgColor
        self.layer.shadowOpacity = 3
        self.layer.shadowOffset = CGSize(width: 3, height: 3)
        self.layer.shadowRadius = 3
        self.layer.shouldRasterize = true
    }    
}
