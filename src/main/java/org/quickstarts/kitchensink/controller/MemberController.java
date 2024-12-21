package org.quickstarts.kitchensink.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.service.MemberRegistrationService;
import org.quickstarts.kitchensink.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private static final String ID_PATTERN = "^[a-fA-F0-9]{24}$";  // Regex for MongoDB ObjectId (24 hex characters)

    private final MemberService memberService;
    private final MemberRegistrationService memberRegistrationService;

    @Autowired
    public MemberController(MemberService memberService, MemberRegistrationService memberRegistrationService) {
        this.memberService = memberService;
        this.memberRegistrationService = memberRegistrationService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createMember(@RequestBody @Valid Member newMember) {
        log.info("Creating member");

        memberService.isEmailExist(newMember.getEmail());

        memberRegistrationService.register(newMember);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Member> listAllMembers() {
        return memberService.findAllOrderedByName();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<Member> lookupMemberById(@PathVariable @Pattern(regexp = ID_PATTERN, message = "Invalid Id format") String id) {
        Member member = memberService.findById(id).orElseThrow();
        return ResponseEntity.ok(member);
    }
}
