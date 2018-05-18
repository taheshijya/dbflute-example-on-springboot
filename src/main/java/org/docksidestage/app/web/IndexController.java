package org.docksidestage.app.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author fukutake
 */
@Controller
@RequestMapping("/")
public class IndexController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(Model model) {

        return "redirect:/mypage";
    }

}
