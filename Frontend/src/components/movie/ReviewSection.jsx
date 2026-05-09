import React, { useState, useEffect } from 'react';
import { Star, MessageCircle, Trash2 } from 'lucide-react';
import { getReviewsByMovie, createReview, deleteReview } from '../../services/api/reviewService';

export default function ReviewSection({ movieId }) {
    const [reviews, setReviews] = useState([]);
    const [rating, setRating] = useState(10);
    const [comment, setComment] = useState('');
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const currentUserStr = localStorage.getItem('user');
    const currentUser = currentUserStr ? JSON.parse(currentUserStr) : null;

    useEffect(() => {
        fetchReviews();
    }, [movieId]);

    const fetchReviews = async () => {
        try {
            setLoading(true);
            const data = await getReviewsByMovie(movieId);
            setReviews(data);
        } catch (err) {
            setError('Không thể tải bình luận');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!currentUser) {
            alert('Bạn cần đăng nhập để đánh giá phim!');
            return;
        }

        try {
            setSubmitting(true);
            const res = await createReview({
                movieId: Number(movieId),
                rating,
                comment
            });
            
            if (res.apiStatus === 'SUCCESS') {
                setComment('');
                setRating(10);
                fetchReviews();
            } else {
                alert(res.message || 'Có lỗi xảy ra');
            }
        } catch (err) {
            alert(err.message || 'Lỗi khi gửi bình luận');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (reviewId) => {
        if (!window.confirm('Bạn có chắc muốn xóa bình luận này?')) return;
        try {
            await deleteReview(reviewId);
            fetchReviews();
        } catch (err) {
            alert(err.message || 'Lỗi khi xóa bình luận');
        }
    };

    return (
        <section className="mt-16 bg-[#111] border border-white/5 p-6 rounded-2xl">
            <h2 className="text-xl font-bold mb-6 flex items-center gap-3">
                <span className="w-8 h-[2px] bg-red-600 rounded-full" />
                Đánh giá từ khán giả
            </h2>

            {/* Comment Form */}
            {currentUser ? (
                <form onSubmit={handleSubmit} className="mb-10 bg-[#1a1a1a] p-4 rounded-xl border border-white/10">
                    <div className="flex items-center gap-4 mb-4">
                        <span className="text-sm text-gray-400">Chấm điểm:</span>
                        <div className="flex gap-1">
                            {[...Array(10)].map((_, i) => (
                                <button
                                    type="button"
                                    key={i}
                                    onClick={() => setRating(i + 1)}
                                    className={`focus:outline-none transition-colors ${i < rating ? 'text-yellow-400' : 'text-gray-600'}`}
                                >
                                    <Star size={20} fill={i < rating ? "currentColor" : "none"} />
                                </button>
                            ))}
                        </div>
                        <span className="font-bold text-yellow-400 ml-2">{rating}/10</span>
                    </div>
                    
                    <textarea
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="Chia sẻ cảm nghĩ của bạn về bộ phim này..."
                        className="w-full bg-black border border-white/10 rounded-lg p-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-red-500 min-h-[100px] mb-4"
                        required
                    />
                    
                    <div className="flex justify-end">
                        <button
                            type="submit"
                            disabled={submitting}
                            className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-6 rounded-lg text-sm transition-colors disabled:opacity-50 flex items-center gap-2"
                        >
                            <MessageCircle size={16} />
                            {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
                        </button>
                    </div>
                </form>
            ) : (
                <div className="mb-10 p-4 bg-[#1a1a1a] rounded-xl border border-white/10 text-center">
                    <p className="text-gray-400 text-sm">Vui lòng <a href="/login" className="text-red-500 hover:underline">đăng nhập</a> để viết đánh giá.</p>
                </div>
            )}

            {/* Comments List */}
            {loading ? (
                <p className="text-gray-500 text-sm text-center py-4">Đang tải đánh giá...</p>
            ) : error ? (
                <p className="text-red-500 text-sm text-center py-4">{error}</p>
            ) : reviews.length === 0 ? (
                <p className="text-gray-500 text-sm text-center py-8">Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
            ) : (
                <div className="space-y-4">
                    {reviews.map(review => (
                        <div key={review.id} className="bg-[#1a1a1a] border border-white/5 p-4 rounded-xl relative group">
                            <div className="flex justify-between items-start mb-2">
                                <div>
                                    <span className="font-bold text-white mr-3">{review.userName}</span>
                                    <span className="text-xs text-gray-500">
                                        {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                                    </span>
                                </div>
                                <div className="flex items-center gap-1 bg-yellow-500/10 px-2 py-1 rounded border border-yellow-500/20">
                                    <Star size={12} fill="currentColor" className="text-yellow-500" />
                                    <span className="text-xs font-bold text-yellow-500">{review.rating}/10</span>
                                </div>
                            </div>
                            <p className="text-sm text-gray-300 leading-relaxed">
                                {review.comment}
                            </p>
                            
                            {/* Delete Button for owner */}
                            {currentUser && currentUser.id === review.userId && (
                                <button
                                    onClick={() => handleDelete(review.id)}
                                    className="absolute top-4 right-20 text-gray-500 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity"
                                    title="Xóa bình luận"
                                >
                                    <Trash2 size={16} />
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}
