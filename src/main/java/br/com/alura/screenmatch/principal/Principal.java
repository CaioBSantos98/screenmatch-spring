package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=4239e345";

    public void exibeMenu() {
        System.out.println("Digite o nome da sériepara busca: ");
        var nomeSerie = leitor.nextLine();

        // Obtendo dados da série na API
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + APIKEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        // Criando e populando uma lista de temporadas com dados da API
        List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(
                    ENDERECO +
                            nomeSerie.replace(" ", "+") +
                            "&season=" +
                            i +
                            APIKEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        // Percorrendo listas dentro de listas com for (nao muito viavel)
//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        // Percorrendo listas dentro de listas com lambdas (mais viavel)
//        temporadas.forEach(temporada ->
//                temporada.episodios().forEach(episodio ->
//                        System.out.println(episodio.titulo())
//                )
//        );


        // Conhecendo as streams e seus métodos
//        List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//
//        nomes.stream()
//                .sorted()
//                .limit(3)
//                .filter(n -> n.startsWith("N"))
//                .map(n -> n.toUpperCase())
//                .forEach(System.out::println);

        // Coletando dados e armazenando em uma variavel com metodos stream
//        List<DadosEpisodio> dadosEpisodiosList = temporadas.stream()
//                .flatMap(temporada -> temporada.episodios().stream())
//                .collect(Collectors.toList());

        // Utilizando o metodo peek para debugar as streams de uma maneira simples
//        System.out.println("\nTop 10 episodios:");
//        dadosEpisodiosList.stream()
//                .filter(dadosEpisodio -> !dadosEpisodio.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(dadosEpisodio -> System.out.println("Primeiro filtro (N/A) " + dadosEpisodio))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(dadosEpisodio -> System.out.println("Ordenação " + dadosEpisodio))
//                .limit(10)
//                .peek(dadosEpisodio -> System.out.println("Limite " + dadosEpisodio))
//                .map(dadosEpisodio -> dadosEpisodio.titulo().toUpperCase())
//                .peek(dadosEpisodio -> System.out.println("Mapeamento " + dadosEpisodio))
//                .forEach(System.out::println);

        // Definindo uma nova lista de episodios a partir da nossa propria Classe
        List<Episodio> episodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream()
                        .map(dadosEpisodio -> new Episodio(temporada.numero(), dadosEpisodio))
                ).collect(Collectors.toList());

//        episodios.forEach(System.out::println);

        // Aplicando filtros utilizando os metodos streams
//        System.out.println("\nDigite um titulo de episodio");
//        var trechoTitulo = leitor.nextLine();
//
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(episodio -> episodio.getTitulo().toLowerCase().contains(trechoTitulo.toLowerCase()))
//                .findFirst();
//        episodioBuscado.ifPresent(System.out::println);
//
//        System.out.println("A partir de que ano voce deseja ver os episodios? ");
//        var ano = leitor.nextInt();
//        leitor.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(episodio -> episodio.getDataLancamento() != null &&
//                        episodio.getDataLancamento().isAfter(dataBusca))
//                .forEach(episodio -> System.out.println(
//                        "Temporada: " + episodio.getTemporada() +
//                                " Episodio: " + episodio.getTitulo() +
//                                " Data lançamento: " + episodio.getDataLancamento().format(formatter)
//                ));


        // Coletando estatisticas usando MAP e DoubleSummaryStatistics
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(episodio -> episodio.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(episodio -> episodio.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episodio: " + est.getMax());
        System.out.println("Pior episodio: " + est.getMin());
        System.out.println("Quantidade de episodios: " + est.getCount());
    }
}
