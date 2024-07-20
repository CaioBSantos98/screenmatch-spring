package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4239e345";
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar serie por titulo
                    5 - Buscar series por ator
                    6 - Buscar top 5 series
                    7 - Buscar series por categoria
                    8 - Filtrar séries
                    9 - Buscar episodio por trecho
                    10 - Top 5 episodios por série
                    11 - Buscar episodios a partir de uma data
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitor.nextInt();
            leitor.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        try {
            Serie serie = new Serie(dados);
            //dadosSeries.add(dados);
            try {
                repositorio.save(serie);
                System.out.println(serie);
            } catch (DataIntegrityViolationException e) {
                System.out.println("Essa série já existe no banco de dados!");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println("Sèrie não encontrada!");
        }
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitor.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitor.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {
            Serie serieEncontrada = serie.get();

            if(serieEncontrada.getEpisodios().isEmpty()) {
                List<DadosTemporada> temporadas = new ArrayList<>();
                for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                    var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                    DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                    temporadas.add(dadosTemporada);
                }
                temporadas.forEach(System.out::println);

                List<Episodio> episodios = temporadas.stream()
                        .flatMap(t -> t.episodios().stream()
                                .map(e -> new Episodio(t.numero(), e)))
                        .collect(Collectors.toList());

                serieEncontrada.setEpisodios(episodios);
                repositorio.save(serieEncontrada);
            } else {
                System.out.println(serieEncontrada.getEpisodios());
            }

        } else {
            System.out.println("Serie não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitor.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome do ator para buscar series: ");
        var nomeAtor = leitor.nextLine();
        System.out.println("Avaliações a partir de que valor de 0 a 10?");
        var avaliacao = leitor.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }


    private void buscarTop5Series() {
        List<Serie> seriesTop5 = repositorio.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }


    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar series de que categoria/genero?");
        var nomeGenero = leitor.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }


    private void filtrarSeriesPorTemporadaEAvaliacao() {
        System.out.println("Filtrar séries até quantas temporadas?");
        var totalTemporadas = leitor.nextInt();
        leitor.nextLine();
        System.out.println("Com avaliação a partir de que valor??");
        var avaliacao = leitor.nextDouble();
        leitor.nextLine();
        List<Serie> seriesFiltradas = repositorio.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);
        System.out.println("\nSeries com até " + totalTemporadas + " temporadas e com avaliação superior a " + avaliacao);
        seriesFiltradas.forEach(s ->
                System.out.println(s.getTitulo() + " - avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episodio para busca?");
        var trechoEpisodio = leitor.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrehco(trechoEpisodio);
        if (episodiosEncontrados.isEmpty()) {
            System.out.println("Nenhum episódio encontrado.");
        } else {
            episodiosEncontrados.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episodio %s - %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumero(), e.getTitulo()));
        }
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episodio %s - %s | Avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumero(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = leitor.nextInt();
            leitor.nextLine();

            List<Episodio> episodiosAno = repositorio.episodiosPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }
}
