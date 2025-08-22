import { api } from "./apiClient";

export const pointsAPI = {
  summary: () => api.get("/api/users/points/summary").then(r => r.data),
  history: () => api.get("/api/point-history").then(r => r.data),
  statistics: () => api.get("/api/point-history/statistics").then(r => r.data),
  redeem:  ({ pointsUsed, rewardType }) =>
    api.post("/api/rewards", { pointsUsed, rewardType }).then(r => r.data),
  rewards: () => api.get("/api/rewards").then(r => r.data),
};
