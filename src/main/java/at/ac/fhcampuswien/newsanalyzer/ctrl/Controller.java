package at.ac.fhcampuswien.newsanalyzer.ctrl;

import at.ac.fhcampuswien.newsanalyzer.downloader.Downloader;
import at.ac.fhcampuswien.newsapi.NewsApi;
import at.ac.fhcampuswien.newsapi.beans.Article;
import at.ac.fhcampuswien.newsapi.beans.NewsResponse;
import at.ac.fhcampuswien.newsapi.beans.Source;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
// In cooperation with Welna & Salvatian :)
public class Controller {

	public static final String APIKEY = "25fe590b3b774181ae0683d247c3b844";  //0038b5ccc1124e94b01d19b0d5982697

	private List<Article> articles = null;

	public String process(NewsApi newsApi) throws NewsAPIException, IllegalArgumentException{
		System.out.println("Start process");

		if(newsApi == null)
			throw new IllegalArgumentException();

		articles = getArticles(newsApi);

		System.out.println("End process");

		return getArticlesPrintReady();
	}

	public List<Article> getArticles(NewsApi newsApi) throws NewsAPIException {
		NewsResponse newsResponse = newsApi.getNews();

		if(!newsResponse.getStatus().equals("ok")){
			throw new NewsAPIException("News Response returned status " + newsResponse.getStatus());
		}
		
		return newsResponse.getArticles();
	}

	private String getArticlesPrintReady() {
		return articles.stream()
				.map(Article::print)
				.collect(Collectors.joining("\n"));
	}

	public long getArticleCount() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load articles first");
		return articles.size();
	}

	public String getSortArticlesByLongestTitle() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load articles first");
		return articles.stream()
				.map(Article::getTitle)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(String::length).reversed())
				.collect(Collectors.joining("\n"));
	}

	public String getShortestNameOfAuthors() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load data first");

		return articles.stream()
				.map(Article::getAuthor)
				.filter(Objects::nonNull)
				.min(Comparator.comparing(String::length))
				.orElseThrow();
	}

	public String getProviderWithMostArticles() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load data first");

		return articles.stream()
				.map(Article::getSource)
				.collect(Collectors.groupingBy(Source::getName))
				.entrySet()
				.stream()
				.max(Comparator.comparingInt(o -> o.getValue().size()))
				.map(stringListEntry -> stringListEntry.getKey() + " " + stringListEntry.getValue().size())
				.orElseThrow();
	}

	private List<String> urlsToList()  {

		return articles.stream()
				.map(Article::getUrl)
				.collect(Collectors.toList());
	}

	public void downloadURL(Downloader downloader)  {

		long begin = System.nanoTime();
		downloader.process(urlsToList());
		long end = System.nanoTime();
		long timePassed = begin - end;

		System.out.println(timePassed + " nanoseconds");
		System.out.println(downloader.getClass().getName());

	}

}
