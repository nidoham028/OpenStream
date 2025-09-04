package org.schabi.newpipe.util;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.nidoham.openstream.R;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public final class ExtractorHelper {
    private static final String TAG = ExtractorHelper.class.getSimpleName();
    private static final InfoCache CACHE = InfoCache.getInstance();

    private static final boolean DEBUG = true; // replaced MainActivity.DEBUG

    private ExtractorHelper() {
        // no instance
    }

    private static void checkServiceId(final int serviceId) {
        if (serviceId == Constants.NO_SERVICE_ID) {
            throw new IllegalArgumentException("serviceId is NO_SERVICE_ID");
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Search, Stream, Channel, Playlist, Comments, Kiosk methods
    //////////////////////////////////////////////////////////////////////////*/

    public static Single<SearchInfo> searchFor(final int serviceId, final String searchString,
                                               final List<String> contentFilter,
                                               final String sortFilter) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                SearchInfo.getInfo(NewPipe.getService(serviceId),
                        NewPipe.getService(serviceId)
                                .getSearchQHFactory()
                                .fromQuery(searchString, contentFilter, sortFilter)));
    }

    public static Single<InfoItemsPage<InfoItem>> getMoreSearchItems(
            final int serviceId,
            final String searchString,
            final List<String> contentFilter,
            final String sortFilter,
            final Page page) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                SearchInfo.getMoreItems(NewPipe.getService(serviceId),
                        NewPipe.getService(serviceId)
                                .getSearchQHFactory()
                                .fromQuery(searchString, contentFilter, sortFilter), page));

    }

    public static Single<List<String>> suggestionsFor(final int serviceId, final String query) {
        checkServiceId(serviceId);
        return Single.fromCallable(() -> {
            final SuggestionExtractor extractor = NewPipe.getService(serviceId)
                    .getSuggestionExtractor();
            return extractor != null
                    ? extractor.suggestionList(query)
                    : Collections.emptyList();
        });
    }

    public static Single<StreamInfo> getStreamInfo(final int serviceId, final String url,
                                                   final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.Type.STREAM,
                Single.fromCallable(() -> StreamInfo.getInfo(NewPipe.getService(serviceId), url)));
    }

    public static Single<ChannelInfo> getChannelInfo(final int serviceId, final String url,
                                                     final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.Type.CHANNEL,
                Single.fromCallable(() ->
                        ChannelInfo.getInfo(NewPipe.getService(serviceId), url)));
    }

    public static Single<ChannelTabInfo> getChannelTab(final int serviceId,
                                                       final ListLinkHandler listLinkHandler,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId,
                listLinkHandler.getUrl(), InfoCache.Type.CHANNEL_TAB,
                Single.fromCallable(() ->
                        ChannelTabInfo.getInfo(NewPipe.getService(serviceId), listLinkHandler)));
    }

    public static Single<InfoItemsPage<InfoItem>> getMoreChannelTabItems(
            final int serviceId,
            final ListLinkHandler listLinkHandler,
            final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                ChannelTabInfo.getMoreItems(NewPipe.getService(serviceId),
                        listLinkHandler, nextPage));
    }

    public static Single<CommentsInfo> getCommentsInfo(final int serviceId,
                                                       final String url,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.Type.COMMENTS,
                Single.fromCallable(() ->
                        CommentsInfo.getInfo(NewPipe.getService(serviceId), url)));
    }

    public static Single<InfoItemsPage<CommentsInfoItem>> getMoreCommentItems(
            final int serviceId,
            final CommentsInfo info,
            final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                CommentsInfo.getMoreItems(NewPipe.getService(serviceId), info, nextPage));
    }

    public static Single<InfoItemsPage<CommentsInfoItem>> getMoreCommentItems(
            final int serviceId,
            final String url,
            final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                CommentsInfo.getMoreItems(NewPipe.getService(serviceId), url, nextPage));
    }

    public static Single<PlaylistInfo> getPlaylistInfo(final int serviceId,
                                                       final String url,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.Type.PLAYLIST,
                Single.fromCallable(() ->
                        PlaylistInfo.getInfo(NewPipe.getService(serviceId), url)));
    }

    public static Single<InfoItemsPage<StreamInfoItem>> getMorePlaylistItems(final int serviceId,
                                                                             final String url,
                                                                             final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                PlaylistInfo.getMoreItems(NewPipe.getService(serviceId), url, nextPage));
    }

    public static Single<KioskInfo> getKioskInfo(final int serviceId,
                                                 final String url,
                                                 final boolean forceLoad) {
        return checkCache(forceLoad, serviceId, url, InfoCache.Type.KIOSK,
                Single.fromCallable(() -> KioskInfo.getInfo(NewPipe.getService(serviceId), url)));
    }

    public static Single<InfoItemsPage<StreamInfoItem>> getMoreKioskItems(final int serviceId,
                                                                          final String url,
                                                                          final Page nextPage) {
        return Single.fromCallable(() ->
                KioskInfo.getMoreItems(NewPipe.getService(serviceId), url, nextPage));
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Cache
    //////////////////////////////////////////////////////////////////////////*/

    private static <I extends Info> Single<I> checkCache(final boolean forceLoad,
                                                         final int serviceId,
                                                         @NonNull final String url,
                                                         @NonNull final InfoCache.Type cacheType,
                                                         @NonNull final Single<I> loadFromNetwork) {
        checkServiceId(serviceId);
        final Single<I> actualLoadFromNetwork = loadFromNetwork
                .doOnSuccess(info -> CACHE.putInfo(serviceId, url, info, cacheType));

        final Single<I> load;
        if (forceLoad) {
            CACHE.removeInfo(serviceId, url, cacheType);
            load = actualLoadFromNetwork;
        } else {
            load = Maybe.concat(ExtractorHelper.loadFromCache(serviceId, url, cacheType),
                            actualLoadFromNetwork.toMaybe())
                    .firstElement()
                    .toSingle();
        }

        return load;
    }

    private static <I extends Info> Maybe<I> loadFromCache(
            final int serviceId,
            @NonNull final String url,
            @NonNull final InfoCache.Type cacheType) {
        checkServiceId(serviceId);
        return Maybe.defer(() -> {
            final I info = (I) CACHE.getFromKey(serviceId, url, cacheType);
            if (DEBUG) {
                Log.d(TAG, "loadFromCache() called, info > " + info);
            }
            if (info != null) {
                return Maybe.just(info);
            }
            return Maybe.empty();
        });
    }

    public static boolean isCached(final int serviceId,
                                   @NonNull final String url,
                                   @NonNull final InfoCache.Type cacheType) {
        return null != loadFromCache(serviceId, url, cacheType).blockingGet();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static void showMetaInfoInTextView(@Nullable final List<MetaInfo> metaInfos,
                                              final TextView metaInfoTextView,
                                              final View metaInfoSeparator,
                                              final CompositeDisposable disposables) {
        final Context context = metaInfoTextView.getContext();
        if (metaInfos == null || metaInfos.isEmpty()
                || !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.show_meta_info_key), true)) {
            metaInfoTextView.setVisibility(View.GONE);
            metaInfoSeparator.setVisibility(View.GONE);

        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final MetaInfo metaInfo : metaInfos) {
                if (!isNullOrEmpty(metaInfo.getTitle())) {
                    stringBuilder.append("<b>").append(metaInfo.getTitle()).append("</b>")
                            .append("."); // replaced Localization.DOT_SEPARATOR
                }

                String content = metaInfo.getContent().getContent().trim();
                if (content.endsWith(".")) {
                    content = content.substring(0, content.length() - 1);
                }
                stringBuilder.append(content);

                for (int i = 0; i < metaInfo.getUrls().size(); i++) {
                    if (i == 0) {
                        stringBuilder.append(".");
                    } else {
                        stringBuilder.append("<br/><br/>");
                    }
                    stringBuilder
                            .append(metaInfo.getUrlTexts().get(i).trim());
                }
            }

            metaInfoSeparator.setVisibility(View.VISIBLE);
            metaInfoTextView.setText(android.text.Html.fromHtml(stringBuilder.toString()));
        }
    }

    private static String capitalizeIfAllUppercase(final String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLowerCase(text.charAt(i))) {
                return text;
            }
        }
        if (text.isEmpty()) {
            return text;
        } else {
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    }
}