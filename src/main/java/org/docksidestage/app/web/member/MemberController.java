package org.docksidestage.app.web.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.util.DfTypeUtil;
import org.docksidestage.bizfw.PagingNavi;
import org.docksidestage.dbflute.allcommon.CDef;
import org.docksidestage.dbflute.exbhv.MemberBhv;
import org.docksidestage.dbflute.exbhv.MemberStatusBhv;
import org.docksidestage.dbflute.exentity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author jflute
 */
@Controller
@RequestMapping("/member")
@SessionAttributes(value = "memberSearchForm")
public class MemberController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    private MemberBhv memberBhv; // #dbflute: you can use DBFlute behaviors like this

    @Autowired
    private MemberStatusBhv memberStatusBhv;

    @ModelAttribute("memberSearchForm")
    MemberSearchForm memberSearchForm() {
        System.out.println("create memberSearchForm");
        return new MemberSearchForm();
    }

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(Model model, MemberForm memberForm) {
        int count = memberBhv.selectCount(cb -> {
            cb.query().setMemberStatusCode_Equal_Formalized();
        });
        logger.debug("count: {}", count);
        return "index";
    }

    // ===================================================================================
    //                                                                           Show List
    //                                                                           =========
    // http://localhost:8080/member/list?pageNumber=1
    // http://localhost:8080/member/list?pageNumber=sea
    @RequestMapping("/list")
    public String list(Model model, @Valid MemberSearchForm form, BindingResult result) {
        logger.debug("#form: {}", form);
        model.addAttribute("memberStatusSelectOption", getMemberStatusSelectOption());
        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            model.addAttribute("beans", Collections.emptyList()); // #for_now avoid error
            // #hope change type failure message
            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // Failed to convert property value of type
            // java.lang.String to required type java.lang.Integer for property pageNumber;
            // nested exception is java.lang.NumberFormatException: For input string: "sea"
            // _/_/_/_/_/_/_/_/_/_/
            return "member/member_list";
        }
        PagingResultBean<Member> page = selectMemberPage(form);
        model.addAttribute("beans", convertToResultBeans(page));

        // ページング用処理
        PagingNavi pagingNavi = new PagingNavi();
        pagingNavi.prepare(page, op -> {
            op.rangeSize(10);
        });
        model.addAttribute("pagingNavi", pagingNavi);

        return "member/member_list";
    }

    protected PagingResultBean<Member> selectMemberPage(MemberSearchForm form) { // #dbflute: you can select like this
        return memberBhv.selectPage(cb -> {
            cb.ignoreNullOrEmptyQuery();
            cb.setupSelect_MemberStatus();
            cb.specify().derivedPurchase().count(purchaseCB -> {
                purchaseCB.specify().columnPurchaseId();
            }, Member.ALIAS_purchaseCount);

            cb.query().setMemberName_LikeSearch(form.getMemberName(), op -> op.likeContain());
            final String purchaseProductName = form.getPurchaseProductName();
            final boolean unpaid = form.unpaid;
            if ((purchaseProductName != null && purchaseProductName.trim().length() > 0) || unpaid) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryProduct().setProductName_LikeSearch(purchaseProductName, op -> op.likeContain());
                    if (unpaid) {
                        purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                    }
                });
            }
            cb.query().setMemberStatusCode_Equal_AsMemberStatus(CDef.MemberStatus.codeOf(form.memberStatus));
            LocalDateTime formalizedDateFrom = DfTypeUtil.toLocalDateTime(form.getFormalizedDateFrom());
            LocalDateTime formalizedDateTo = DfTypeUtil.toLocalDateTime(form.formalizedDateTo);
            cb.query().setFormalizedDatetime_FromTo(formalizedDateFrom, formalizedDateTo, op -> op.compareAsDate());

            cb.query().addOrderBy_UpdateDatetime_Desc();
            cb.query().addOrderBy_MemberId_Asc();

            int pageSize = 4;
            cb.paging(pageSize, form.getPageNumber());
        });
    }

    protected List<MemberSearchRowBean> convertToResultBeans(PagingResultBean<Member> page) {
        List<MemberSearchRowBean> beanList = page.stream().map(member -> {
            MemberSearchRowBean bean = new MemberSearchRowBean();
            bean.setMemberId(member.getMemberId());
            bean.memberName = member.getMemberName();
            member.getMemberStatus().alwaysPresent(status -> {
                bean.memberStatusName = status.getMemberStatusName();
            });
            bean.formalizedDate = DfTypeUtil.toStringDate(member.getFormalizedDatetime(), "yyyy/MM/dd");
            bean.updateDatetime = DfTypeUtil.toStringDate(member.getUpdateDatetime(), "yyyy/MM/dd");
            bean.withdrawalMember = member.isMemberStatusCodeWithdrawal();
            bean.purchaseCount = member.getPurchaseCount();
            return bean;
        }).collect(Collectors.toList());
        return beanList;
    }

    // ===================================================================================
    //                                                                          Add Member
    //                                                                          ==========
    @RequestMapping("/add")
    public String add(Model model, MemberAddForm memberAddForm, BindingResult result) {
        model.addAttribute("memberStatusSelectOption", getMemberStatusSelectOption());
        return "member/member_add";
    }

    @RequestMapping("/add/register")
    public String register(Model model, @Valid MemberAddForm memberAddForm, BindingResult result) {
        model.addAttribute("memberStatusSelectOption", getMemberStatusSelectOption());

        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            return "member/member_add";
        }

        insertMember(memberAddForm);

        return "redirect:/member/list";
    }

    // ===================================================================================
    //                                                                          Edit Member
    //
    @RequestMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model, MemberEditForm memberEditForm, BindingResult result) {
        model.addAttribute("memberStatusSelectOption", getMemberStatusSelectOption());

        Member member = selectMember(id);
        mappingToForm(member, memberEditForm);

        return "member/member_edit";
    }

    @RequestMapping("/update")
    public String update(Model model, @Valid MemberEditForm memberEditForm, BindingResult result) {
        model.addAttribute("memberStatusSelectOption", getMemberStatusSelectOption());

        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            return "member/member_edit";
        }

        Member member = updateMember(memberEditForm);

        return "redirect:/member/edit/" + member.getMemberId();
    }

    protected Map<String, String> getMemberStatusSelectOption() {

        Map<String, String> memberStatusSelectOption = new LinkedHashMap<String, String>();
        memberStatusBhv.selectList(cb -> {
            cb.query().addOrderBy_DisplayOrder_Asc();
        }).forEach(action -> {
            memberStatusSelectOption.put(action.getMemberStatusCode(), action.getMemberStatusName());
        });

        return memberStatusSelectOption;
    }

    // ===================================================================================
    //                                                                              insert
    //                                                                              ======
    private void insertMember(MemberAddForm form) {
        Member member = new Member();
        member.setMemberName(form.getMemberName());
        member.setMemberAccount(form.getMemberAccount());
        member.setBirthdate(LocalDate.parse(form.getBirthdate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        member.setMemberStatusCodeAsMemberStatus(CDef.MemberStatus.codeOf(form.getMemberStatus()));
        if (member.isMemberStatusCodeFormalized()) {
            member.setFormalizedDatetime(LocalDateTime.now());
        }
        memberBhv.insert(member);
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private void mappingToForm(Member member, MemberEditForm form) {
        form.setMemberId(member.getMemberId());
        form.setMemberName(member.getMemberName());
        form.setMemberAccount(member.getMemberAccount());
        form.setMemberStatus(member.getMemberStatusCode());
        if (member.getBirthdate() != null) {
            form.setBirthdate(member.getBirthdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
        if (formalizedDatetime != null) {
            form.setFormalizedDate(formalizedDatetime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (member.getLatestLoginDatetime() != null) {
            form.setLatestLoginDatetime(member.getLatestLoginDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        form.setUpdateDatetime(member.getUpdateDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        form.setPreviousStatus(member.getMemberStatusCode()); // to determine new formalized member
        form.setVersionNo(member.getVersionNo());
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private Member updateMember(MemberEditForm form) {
        Member member = new Member();
        member.setMemberId(form.getMemberId());
        member.setMemberName(form.getMemberName());
        if (form.getBirthdate() != null && !form.getBirthdate().isEmpty()) {
            member.setBirthdate(LocalDate.parse(form.getBirthdate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // may be updated as null
        }
        member.setMemberStatusCodeAsMemberStatus(CDef.MemberStatus.codeOf(form.getMemberStatus()));
        member.setMemberAccount(form.getMemberAccount());
        if (member.isMemberStatusCodeFormalized()) {
            if (CDef.MemberStatus.codeOf(form.getPreviousStatus()).isShortOfFormalized()) {
                member.setFormalizedDatetime(LocalDateTime.now());
            }
        } else if (member.isMemberStatusCode_ShortOfFormalized()) {
            member.setFormalizedDatetime(null);
        }
        member.setVersionNo(form.getVersionNo());
        memberBhv.update(member);
        return member;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private Member selectMember(Integer memberId) {
        return memberBhv.selectEntity(cb -> {
            cb.specify().derivedMemberLogin().max(loginCB -> {
                loginCB.specify().columnLoginDatetime();
            }, Member.ALIAS_latestLoginDatetime);
            cb.query().setMemberId_Equal(memberId);
            cb.query().setMemberStatusCode_InScope_ServiceAvailable();
        }).get(); // automatically exclusive controlled if not found
    }
}
