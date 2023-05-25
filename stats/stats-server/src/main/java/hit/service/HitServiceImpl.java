package hit.service;

import hit.dto.EndpointHitDto;
import hit.mapper.EndpointHitMapper;
import hit.model.EndpointHit;
import hit.repo.HitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Autowired
    private EndpointHitMapper hitMapper;

    @Override
    public EndpointHitDto addNewHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = hitMapper.toEndpointHit(endpointHitDto);
        EndpointHit savedEndpointHit = hitRepository.save(endpointHit);
        log.info("Добавлен новый просмотр {}", endpointHit);
        return hitMapper.toEndpointHitDto(savedEndpointHit);
    }

}