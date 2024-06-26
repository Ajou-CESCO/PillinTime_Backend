package com.cesco.pillintime.api.medicine.controller;

import com.cesco.pillintime.api.medicine.dto.MedicineDto;
import com.cesco.pillintime.api.medicine.service.MedicineService;
import com.cesco.pillintime.response.dto.ResponseDto;
import com.cesco.pillintime.response.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicine")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<ResponseDto> getMedicineInfo(@RequestParam(name = "name") String name,@RequestParam(name = "memberId") Long memberId) {
        List<MedicineDto> medicineDtoList = medicineService.getMedicineInfoByName(name, memberId);
        return ResponseUtil.makeResponse(200, "Success get medicine", medicineDtoList);
    }

    @GetMapping("/{medicineId}")
    public ResponseEntity<ResponseDto> getMedicineByMedicineId(@PathVariable int medicineId) {
        MedicineDto medicineDto = medicineService.getMedicineInfoByMedicineId(medicineId);
        return ResponseUtil.makeResponse(200, "Success get medicine", medicineDto);
    }
}
