import React, { useState, useEffect } from 'react';
import { 
  HelpCircle, 
  Plus, 
  Trash2, 
  Edit, 
  CheckCircle, 
  XCircle, 
  Clock, 
  Layers, 
  RefreshCw,
  X,
  AlertCircle,
  Check,
  Radio
} from 'lucide-react';
import { api } from '../../api/client';
import { Quiz } from '../../types';

export const QuizzesView: React.FC = () => {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingQuiz, setEditingQuiz] = useState<Quiz | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // Form Fields
  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState<string[]>(['', '', '', '']);
  const [correctIndex, setCorrectIndex] = useState(0);
  const [timerSeconds, setTimerSeconds] = useState(10);
  const [category, setCategory] = useState('General');
  const [isActive, setIsActive] = useState(true);
  const [order, setOrder] = useState(1);

  // Delete modal
  const [deletingId, setDeletingId] = useState<string | null>(null);

  useEffect(() => {
    fetchQuizzes();
  }, []);

  const fetchQuizzes = async () => {
    try {
      setLoading(true);
      const res = await api.getQuizzes();
      setQuizzes(res.quizzes);
    } catch (err) {
      console.error('Failed to fetch quizzes', err);
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setEditingQuiz(null);
    setQuestion('');
    setOptions(['', '', '', '']);
    setCorrectIndex(0);
    setTimerSeconds(10);
    setCategory('General');
    setIsActive(true);
    setOrder(quizzes.length + 1);
    setFormError(null);
    setIsModalOpen(true);
  };

  const openEditModal = (q: Quiz) => {
    setEditingQuiz(q);
    setQuestion(q.question);
    setOptions([...q.options]);
    setCorrectIndex(q.correctOptionIndex);
    setTimerSeconds(q.timerSeconds || 10);
    setCategory(q.category || 'General');
    setIsActive(q.isActive);
    setOrder(q.order || 1);
    setFormError(null);
    setIsModalOpen(true);
  };

  const handleOptionChange = (idx: number, val: string) => {
    const updated = [...options];
    updated[idx] = val;
    setOptions(updated);
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) {
      setFormError('Please enter a valid question.');
      return;
    }
    const cleanOptions = options.map(o => o.trim()).filter(o => o.length > 0);
    if (cleanOptions.length < 2) {
      setFormError('Please provide at least 2 non-empty options.');
      return;
    }
    if (correctIndex >= cleanOptions.length) {
      setFormError('Please select a valid correct option.');
      return;
    }

    setSubmitting(true);
    setFormError(null);

    try {
      if (editingQuiz) {
        await api.updateQuiz(editingQuiz.id, {
          question: question.trim(),
          options: cleanOptions,
          correctOptionIndex: correctIndex,
          timerSeconds,
          category: category.trim(),
          isActive,
          order
        });
      } else {
        await api.createQuiz({
          question: question.trim(),
          options: cleanOptions,
          correctOptionIndex: correctIndex,
          timerSeconds,
          category: category.trim(),
          isActive,
          order
        });
      }
      setIsModalOpen(false);
      fetchQuizzes();
    } catch (err: any) {
      setFormError(err.message || 'Failed to save quiz');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleActive = async (id: string) => {
    try {
      await api.toggleQuiz(id);
      fetchQuizzes();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingId) return;
    try {
      await api.deleteQuiz(deletingId);
      setDeletingId(null);
      fetchQuizzes();
    } catch (err) {
      console.error(err);
    }
  };

  const categories = Array.from(new Set(quizzes.map(q => q.category || 'General')));

  const filteredQuizzes = quizzes.filter(q => {
    const matchesSearch = !search || q.question.toLowerCase().includes(search.toLowerCase());
    const matchesCat = !categoryFilter || q.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  return (
    <div className="space-y-6">
      {/* Header with Title and Add Button */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Tasks & Quizzes Management ({quizzes.length})
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Configure task questions, 10-second countdown timers, and answer choices.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchQuizzes}
            disabled={loading}
            className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white hover:bg-slate-800 transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={openAddModal}
            className="flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-purple-600/30 transition"
          >
            <Plus className="w-4 h-4" />
            Add New Quiz
          </button>
        </div>
      </div>

      {/* Rules Info Banner */}
      <div className="bg-purple-950/30 border border-purple-800/40 p-4 rounded-2xl flex flex-col md:flex-row items-start md:items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-purple-500/20 text-purple-400 flex items-center justify-center font-bold shrink-0">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="font-bold text-white">10-Second Anti-Fraud Timer:</span> Each quiz enforces a strict 10s countdown on the User App before completion.
            <div className="text-slate-400 mt-0.5">
              5 consecutive valid quizzes unlock the Start.io Rewarded Video Ad to claim 1 reward point.
            </div>
          </div>
        </div>
        <div className="shrink-0 bg-purple-900/50 px-3 py-1.5 rounded-lg border border-purple-700/40 text-[11px] text-purple-200 font-mono">
          5 Quizzes / Cycle = 1 Point
        </div>
      </div>

      {/* Search and Filters */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col sm:flex-row gap-3 items-center justify-between">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search quiz question text..."
          className="w-full sm:w-80 bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-xs text-white placeholder:text-slate-500 focus:outline-none focus:border-purple-500"
        />

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-300 rounded-xl px-3 py-2 text-xs focus:outline-none focus:border-purple-500"
          >
            <option value="">All Categories</option>
            {categories.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Quiz Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {loading ? (
          <div className="col-span-2 text-center py-16">
            <div className="inline-block w-8 h-8 border-4 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
          </div>
        ) : filteredQuizzes.length === 0 ? (
          <div className="col-span-2 text-center py-16 text-xs text-slate-500 bg-slate-900 border border-slate-800 rounded-2xl">
            No quizzes found. Click "Add New Quiz" to create one.
          </div>
        ) : (
          filteredQuizzes.map(quiz => (
            <div
              key={quiz.id}
              className={`bg-slate-900 border rounded-2xl p-5 flex flex-col justify-between transition ${
                quiz.isActive ? 'border-slate-800' : 'border-slate-800/40 opacity-60'
              }`}
            >
              <div>
                {/* Top Badge & Order */}
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="bg-purple-500/10 text-purple-400 border border-purple-500/20 px-2 py-0.5 rounded text-[10px] font-bold font-mono">
                      #{quiz.order || 1} • {quiz.category || 'General'}
                    </span>
                    <span className="flex items-center gap-1 text-[10px] text-slate-400">
                      <Clock className="w-3 h-3" /> {quiz.timerSeconds || 10}s timer
                    </span>
                  </div>

                  <button
                    onClick={() => handleToggleActive(quiz.id)}
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full transition ${
                      quiz.isActive 
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' 
                        : 'bg-slate-800 text-slate-400 border border-slate-700'
                    }`}
                  >
                    {quiz.isActive ? 'Active' : 'Disabled'}
                  </button>
                </div>

                {/* Question */}
                <h3 className="text-sm font-bold text-white mb-3">
                  {quiz.question}
                </h3>

                {/* Options List */}
                <div className="space-y-1.5 mb-4">
                  {quiz.options.map((opt, idx) => {
                    const isCorrect = idx === quiz.correctOptionIndex;
                    return (
                      <div
                        key={idx}
                        className={`p-2 rounded-xl text-xs flex items-center justify-between border ${
                          isCorrect
                            ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300 font-bold'
                            : 'bg-slate-950/60 border-slate-800 text-slate-400'
                        }`}
                      >
                        <span className="flex items-center gap-2">
                          <span className="w-4 h-4 rounded-full bg-slate-800 text-slate-300 flex items-center justify-center text-[10px] font-mono">
                            {String.fromCharCode(65 + idx)}
                          </span>
                          <span>{opt}</span>
                        </span>
                        {isCorrect && (
                          <span className="text-[10px] text-emerald-400 font-bold flex items-center gap-1">
                            <Check className="w-3 h-3" /> Correct
                          </span>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Bottom Actions */}
              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-800/80">
                <button
                  onClick={() => openEditModal(quiz)}
                  className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition text-xs flex items-center gap-1"
                >
                  <Edit className="w-3.5 h-3.5" />
                  <span>Edit</span>
                </button>
                <button
                  onClick={() => setDeletingId(quiz.id)}
                  className="p-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/20 transition text-xs flex items-center gap-1"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Delete</span>
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Add / Edit Quiz Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <HelpCircle className="w-5 h-5 text-purple-400" />
                {editingQuiz ? 'Edit Quiz Question' : 'Create New Task Quiz'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {formError && (
              <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleFormSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Question Text *
                </label>
                <textarea
                  required
                  rows={2}
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  placeholder="e.g. What is the official token standard for BNB Smart Chain?"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-300 mb-1">
                    Category
                  </label>
                  <input
                    type="text"
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    placeholder="Crypto / Tech / Math..."
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-purple-500"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-300 mb-1">
                    Timer (Seconds)
                  </label>
                  <input
                    type="number"
                    min="5"
                    max="60"
                    value={timerSeconds}
                    onChange={(e) => setTimerSeconds(parseInt(e.target.value) || 10)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-purple-500"
                  />
                </div>
              </div>

              {/* Options & Radio Selector */}
              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Answer Options (Select the radio of the correct answer) *
                </label>
                <div className="space-y-2">
                  {options.map((opt, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => setCorrectIndex(idx)}
                        className={`w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs shrink-0 transition ${
                          correctIndex === idx 
                            ? 'bg-emerald-500 text-slate-950' 
                            : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
                        }`}
                        title="Mark as correct answer"
                      >
                        {String.fromCharCode(65 + idx)}
                      </button>
                      <input
                        type="text"
                        required
                        value={opt}
                        onChange={(e) => handleOptionChange(idx, e.target.value)}
                        placeholder={`Option ${String.fromCharCode(65 + idx)}...`}
                        className={`flex-1 bg-slate-950 border rounded-xl px-3 py-2 text-white focus:outline-none ${
                          correctIndex === idx ? 'border-emerald-500/50' : 'border-slate-800 focus:border-purple-500'
                        }`}
                      />
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between pt-2">
                <label className="flex items-center gap-2 cursor-pointer text-slate-300 font-bold">
                  <input
                    type="checkbox"
                    checked={isActive}
                    onChange={(e) => setIsActive(e.target.checked)}
                    className="rounded border-slate-700 text-purple-600 focus:ring-purple-500 w-4 h-4 bg-slate-950"
                  />
                  <span>Publish & Activate Quiz</span>
                </label>

                <div className="flex items-center gap-1.5">
                  <span className="text-slate-400">Order:</span>
                  <input
                    type="number"
                    value={order}
                    onChange={(e) => setOrder(parseInt(e.target.value) || 1)}
                    className="w-16 bg-slate-950 border border-slate-800 rounded-lg px-2 py-1 text-center text-white"
                  />
                </div>
              </div>

              <div className="flex gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-bold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl font-bold transition disabled:opacity-50"
                >
                  {submitting ? 'Saving...' : 'Save Quiz'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deletingId && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-sm w-full p-6 shadow-2xl">
            <h3 className="text-base font-bold text-white mb-2 flex items-center gap-2">
              <Trash2 className="w-4 h-4 text-red-400" />
              Delete Task Quiz
            </h3>
            <p className="text-xs text-slate-400 mb-4">
              Are you sure you want to permanently remove this quiz from the active task pool?
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setDeletingId(null)}
                className="flex-1 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-bold"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                className="flex-1 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-bold"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
