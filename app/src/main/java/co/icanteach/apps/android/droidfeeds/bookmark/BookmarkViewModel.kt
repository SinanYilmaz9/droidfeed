package co.icanteach.apps.android.droidfeeds.bookmark

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.icanteach.apps.android.droidfeeds.analytics.AnalyticsKeys
import co.icanteach.apps.android.droidfeeds.analytics.AnalyticsUseCase
import co.icanteach.apps.android.droidfeeds.bookmark.domain.BookmarkActionsUseCase
import co.icanteach.apps.android.droidfeeds.bookmark.domain.FetchBookmarksUseCase
import co.icanteach.apps.android.droidfeeds.core.*
import co.icanteach.apps.android.droidfeeds.home.domain.HomeFeedListing
import co.icanteach.apps.android.droidfeeds.news.NewsItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class BookmarkViewModel @ViewModelInject constructor(
    private val useCase: FetchBookmarksUseCase,
    private val bookmarkActionsUseCase: BookmarkActionsUseCase,
    private val analyticsUseCase: AnalyticsUseCase
) : ViewModel() {

    private val homeFeedListing = MutableLiveData<HomeFeedListing>()
    val homeFeedListing_: LiveData<HomeFeedListing> = homeFeedListing

    private val statusState = MutableLiveData<StatusViewState>()
    val status_: LiveData<StatusViewState> = statusState

    val bookmarkSuccessResult: ActionEvent = ActionEvent()

    init {
        fetchHomeFeed()
        analyticsUseCase.sendScreenView(AnalyticsKeys.PAGE.HOME)
    }

    private fun fetchHomeFeed() {

        useCase.fetchContent()
            .doOnSuccess { data ->
                homeFeedListing.value = data
            }
            .doOnStatusChanged { status ->
                statusState.value = StatusViewState(status)
            }
            .launchIn(viewModelScope)
    }

    fun removeBookmark(newsItem: NewsItem) {

        analyticsUseCase.sendClickEvent(AnalyticsKeys.CLICK.REMOVE, AnalyticsKeys.PAGE.READING_LIST)

        viewModelScope.launch {
            bookmarkActionsUseCase
                .removeBookmark(
                    originUrl = newsItem.originUrl,
                    title = newsItem.title,
                    description = newsItem.description,
                    image = newsItem.image
                ).doOnLoading {
                    statusState.value = StatusViewState(Status.ContentWithLoading)
                }.doOnSuccess {
                    bookmarkSuccessResult.call()
                    statusState.value = StatusViewState(Status.Content)
                    fetchHomeFeed()
                }.doOnError {
                    statusState.value = StatusViewState(Status.Content)
                }
                .launchIn(viewModelScope)
        }
    }
}