package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository repositorio;

    public List<SerieDTO> obterTodasAsSeries() {
        return converteDados(repositorio.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return converteDados(repositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie = repositorio.findById(id);
        if(serie.isPresent()) {
            var s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<SerieDTO> obterLancamentos() {
        return converteDados(repositorio.lancamentosMaisRecentes());
    }

    public List<EpisodioDTO> obterTodasAsTemporadas(Long id) {
        Optional<Serie> serie = repositorio.findById(id);
        if(serie.isPresent()) {
            var s = serie.get();
            return s.getEpisodios().stream()
                    .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumero()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporada(Long id, Long numero) {
        return repositorio.obterEpisodiosPorTemporada(id, numero)
                .stream()
                .map(episodio -> new EpisodioDTO(episodio.getTemporada(), episodio.getTitulo(), episodio.getNumero()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        return converteDados(repositorio.findByGenero(categoria));
    }

    public List<EpisodioDTO> obterTop5Episodios(Long id) {
        return repositorio.obterTop5Episodios(id).stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumero()))
                .collect(Collectors.toList());
    }

    private List<SerieDTO> converteDados(List<Serie> series) {
        return series
                .stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
                .collect(Collectors.toList());
    }
}
