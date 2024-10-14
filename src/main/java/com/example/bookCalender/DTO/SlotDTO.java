package com.example.bookCalender.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDTO {

        private LocalDateTime startTime;
        private LocalDateTime endTime;

}
