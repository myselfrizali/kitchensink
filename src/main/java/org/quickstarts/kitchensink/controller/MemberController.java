package org.quickstarts.kitchensink.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.quickstarts.kitchensink.exception.MemberNotFoundException;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.pojo.MemberDTO;
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
    public ResponseEntity<Member> createMember(@RequestBody @Valid MemberDTO newMemberRequest) throws MemberNotFoundException {
        log.info("Creating member");

        memberService.isEmailExist(newMemberRequest.getEmail());

        Member newMember = new Member(newMemberRequest.getName(), newMemberRequest.getEmail(), newMemberRequest.getPhoneNumber());
        memberRegistrationService.register(newMember);
        Member member = memberService.findByEmail(newMember.getEmail()).orElseThrow(MemberNotFoundException::new);

        return ResponseEntity.ok().body(member);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Member> listAllMembers() {
        return memberService.findAllOrderedByName().stream()
                .filter(member -> !member.isDeleted())
                .toList();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<Member> lookupMemberById(@PathVariable @Pattern(regexp = ID_PATTERN, message = "Invalid Id format") String id) throws MemberNotFoundException {
        Member member = memberService.findById(id).orElseThrow(MemberNotFoundException::new);
        if (member.isDeleted() || !member.isActive()) {
            throw new MemberNotFoundException();
        }

        return ResponseEntity.ok(member);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable @Pattern(regexp = ID_PATTERN, message = "Invalid Id format") String id) throws MemberNotFoundException {

        log.info("Deleting member with ID: " + id);

        // Check if the member exists
        Member existingMember = memberService.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        if (existingMember.isDeleted() || !existingMember.isActive()) {
            throw new MemberNotFoundException();
        }

        memberService.delete(existingMember);  // Delete the member

        return ResponseEntity.ok("Member successfully deleted");
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/status")
    public ResponseEntity<String> changeMemberStatus(@PathVariable @Pattern(regexp = ID_PATTERN, message = "Invalid Id format") String id) throws MemberNotFoundException {

        log.info("Changing status for member with ID: " + id);

        Member existingMember = memberService.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        existingMember.setActive(!existingMember.isActive());
        memberService.save(existingMember);

        return ResponseEntity.ok("Member status updated");
    }

}
